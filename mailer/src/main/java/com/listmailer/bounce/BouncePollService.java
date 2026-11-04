package com.listmailer.bounce;

import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.catalogs.BounceMailboxEntity;
import com.listmailer.catalogs.BounceMailboxRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.mailbox.MailboxPollLock;
import com.listmailer.mailbox.MailboxQueryException;
import com.listmailer.noticemail.AdminNoticeMail;
import com.listmailer.ops.ActivityLogService;
import com.listmailer.secrets.SecretStore;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BouncePollService {

  private static final Logger log = LoggerFactory.getLogger(BouncePollService.class);

  private final BounceMailboxRepository mailboxes;
  private final BounceEventRepository events;
  private final MailboxPort mailbox;
  private final OpsHooksPort ops;
  private final ActivityLogService activity;
  private final MailboxPollLock locks;
  private final SecretStore secrets;
  private final BounceCorrelationService correlation;
  private final SendQueueRepository queue;
  private final CampaignRepository campaigns;
  private final AdminNoticeMail adminNotices;
  private final ExecutorService async = Executors.newCachedThreadPool();

  public BouncePollService(
      BounceMailboxRepository mailboxes,
      BounceEventRepository events,
      MailboxPort mailbox,
      OpsHooksPort ops,
      ActivityLogService activity,
      MailboxPollLock locks,
      SecretStore secrets,
      BounceCorrelationService correlation,
      SendQueueRepository queue,
      CampaignRepository campaigns,
      AdminNoticeMail adminNotices) {
    this.mailboxes = mailboxes;
    this.events = events;
    this.mailbox = mailbox;
    this.ops = ops;
    this.activity = activity;
    this.locks = locks;
    this.secrets = secrets;
    this.correlation = correlation;
    this.queue = queue;
    this.campaigns = campaigns;
    this.adminNotices = adminNotices;
  }

  /** Sequential poll of due bounce mailboxes. */
  public int pollDue() {
    Instant now = Instant.now();
    List<BounceMailboxEntity> dueBoxes = new ArrayList<>();
    for (BounceMailboxEntity box : mailboxes.findAll()) {
      if (box.isEnabled() && due(box, now)) {
        dueBoxes.add(box);
      }
    }
    if (dueBoxes.isEmpty()) {
      return 0;
    }
    activity.write(ActivityLogService.BOUNCE, "START", "bounce poll mailboxes=" + dueBoxes.size());
    List<String> ingestedIds = new ArrayList<>();
    try {
      for (BounceMailboxEntity box : dueBoxes) {
        if (!tryStart(box.getId())) {
          continue;
        }
        ingestedIds.addAll(pollOne(box.getId()));
      }
      return ingestedIds.size();
    } finally {
      activity.write(ActivityLogService.BOUNCE, "END", "bounce poll ingested=" + ingestedIds.size());
      if (!ingestedIds.isEmpty()) {
        notifyBouncesReceived(ingestedIds);
      }
    }
  }

  /** Start a poll of one mailbox on a background thread. Returns false if lock was already held. */
  public boolean startPollAsync(String id) {
    if (!tryStart(id)) {
      return false;
    }
    async.execute(
        () -> {
          List<String> ids = pollOne(id);
          if (!ids.isEmpty()) {
            notifyBouncesReceived(ids);
          }
        });
    return true;
  }

  @Transactional
  public void breakLock(String id) {
    BounceMailboxEntity box =
        mailboxes.findById(id).orElseThrow(() -> new IllegalArgumentException("not found"));
    box.setPolling(false);
    mailboxes.save(box);
  }

  private boolean due(BounceMailboxEntity box, Instant now) {
    if (box.getLastPolledAt() == null) {
      return true;
    }
    return box.getLastPolledAt()
        .plus(Math.max(1, box.getPollIntervalMinutes()), ChronoUnit.MINUTES)
        .isBefore(now);
  }

  private boolean tryStart(String id) {
    BounceMailboxEntity box = mailboxes.findById(id).orElse(null);
    if (box == null) {
      return false;
    }
    Instant now = Instant.now();
    if (locks.stillHeld(box.isPolling(), box.getLastPollingUpdate(), now)) {
      return false;
    }
    box.setPolling(true);
    box.setLastPollingUpdate(now);
    mailboxes.save(box);
    return true;
  }

  private void heartbeat(BounceMailboxEntity box) {
    box.setLastPollingUpdate(Instant.now());
    mailboxes.save(box);
  }

  private void finish(BounceMailboxEntity box, boolean ok) {
    Instant now = Instant.now();
    box.setPolling(false);
    box.setLastPollingUpdate(now);
    box.setLastPolledAt(now);
    box.setLastPollOk(ok);
    mailboxes.save(box);
  }

  /** @return ids of newly ingested bounce events */
  List<String> pollOne(String id) {
    BounceMailboxEntity box = mailboxes.findById(id).orElse(null);
    if (box == null) {
      return List.of();
    }
    Instant now = Instant.now();
    List<String> ingestedIds = new ArrayList<>();
    AtomicInteger count = new AtomicInteger();
    heartbeat(box);
    activity.write(ActivityLogService.BOUNCE, "CONNECT", mailboxRef(box));
    try {
      mailbox.poll(
          new MailboxPort.MailboxConn(
              box.getHost(),
              box.getPort(),
              box.getUsername(),
              secrets.requirePlain(box.getPasswordSecretId()),
              box.isUseSsl(),
              box.isUseTls(),
              "INBOX"),
          raw -> {
            String eventId = Guids.newGuid();
            String headersJson = headersToText(raw.headers());
            events.save(
                new BounceEventEntity(
                    eventId,
                    box.getId(),
                    now,
                    raw.messageId(),
                    raw.subject(),
                    raw.from(),
                    headersJson,
                    raw.bytes(),
                    "INGESTED"));
            ingestedIds.add(eventId);
            count.incrementAndGet();
            heartbeat(box);
            activity.write(
                ActivityLogService.BOUNCE,
                "FETCH",
                mailboxRef(box)
                    + " from="
                    + raw.from()
                    + " subject="
                    + raw.subject()
                    + " messageId="
                    + raw.messageId());
            return MailboxPort.HandleResult.SEEN;
          });
      finish(box, true);
      activity.write(ActivityLogService.BOUNCE, "DISCONNECT", mailboxRef(box));
      if (count.get() > 0) {
        ops.opsLog(
            "bounce.poll",
            "mailbox=" + box.getName() + " id=" + box.getId() + " ingested=" + count.get());
      }
    } catch (MailboxQueryException ex) {
      finish(box, false);
      activity.write(
          ActivityLogService.BOUNCE,
          "DISCONNECT",
          mailboxRef(box) + " error=" + ex.getMessage());
      ops.opsLog("bounce.poll", "mailbox=" + box.getName() + " error=" + ex.getMessage());
      ops.attentionAdmin("Bounce mailbox poll failed", box.getName() + ": " + ex.getMessage());
      log.warn("bounce poll {}: {}", box.getName(), ex.getMessage());
    } catch (RuntimeException ex) {
      finish(box, false);
      throw ex;
    }
    return ingestedIds;
  }

  void notifyBouncesReceived(List<String> eventIds) {
    try {
      correlation.correlatePending();
      Map<String, Integer> byCampaign = new LinkedHashMap<>();
      for (String id : eventIds) {
        BounceEventEntity ev = events.findById(id).orElse(null);
        if (ev == null) {
          continue;
        }
        String label = "Uncorrelated";
        if (ev.getSendQueueId() != null) {
          SendQueueEntity row = queue.findById(ev.getSendQueueId()).orElse(null);
          if (row != null) {
            label =
                campaigns
                    .findById(row.getCampaignId())
                    .map(CampaignEntity::getSubject)
                    .map(s -> s + " (" + row.getCampaignId() + ")")
                    .orElse(row.getCampaignId());
          }
        }
        byCampaign.merge(label, 1, Integer::sum);
      }
      StringBuilder text = new StringBuilder("Bounce emails received:\n");
      StringBuilder html = new StringBuilder("<p>Bounce emails received:</p><ul>");
      for (Map.Entry<String, Integer> e : byCampaign.entrySet()) {
        text.append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        html.append("<li>")
            .append(e.getKey())
            .append(": ")
            .append(e.getValue())
            .append("</li>");
      }
      html.append("</ul>");
      adminNotices.send("Bounce(s) Received", text.toString(), html.toString());
    } catch (RuntimeException ignored) {
      // never fail poll for notice mail
    }
  }

  /** Keep mailboxId= for per-box log filters; name is for operators. */
  static String mailboxRef(BounceMailboxEntity box) {
    return "mailbox=" + box.getName() + " mailboxId=" + box.getId();
  }

  static String headersToText(Map<String, String> headers) {
    if (headers == null || headers.isEmpty()) {
      return "";
    }
    return headers.entrySet().stream()
        .map(e -> e.getKey() + ": " + e.getValue())
        .collect(Collectors.joining("\n"));
  }
}
