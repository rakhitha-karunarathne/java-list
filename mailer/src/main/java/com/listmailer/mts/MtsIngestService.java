package com.listmailer.mts;

import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.mailbox.MailboxPollLock;
import com.listmailer.mailbox.MailboxQueryException;
import com.listmailer.mailbox.MimeBodyParser;
import com.listmailer.ops.ActivityLogService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MtsIngestService {

  private static final Logger log = LoggerFactory.getLogger(MtsIngestService.class);

  private final MtsInboxRepository inboxes;
  private final MtsAuthorizedSenderRepository senders;
  private final MtsSenderListGrantRepository grants;
  private final MailboxPort mailbox;
  private final MtsGateService gates;
  private final MtsCampaignFactory campaigns;
  private final MtsFromDomainService fromDomain;
  private final MtsDropService drops;
  private final OpsHooksPort ops;
  private final ActivityLogService activity;
  private final MailboxPollLock locks;
  private final ExecutorService async = Executors.newCachedThreadPool();

  public MtsIngestService(
      MtsInboxRepository inboxes,
      MtsAuthorizedSenderRepository senders,
      MtsSenderListGrantRepository grants,
      MailboxPort mailbox,
      MtsGateService gates,
      MtsCampaignFactory campaigns,
      MtsFromDomainService fromDomain,
      MtsDropService drops,
      OpsHooksPort ops,
      ActivityLogService activity,
      MailboxPollLock locks) {
    this.inboxes = inboxes;
    this.senders = senders;
    this.grants = grants;
    this.mailbox = mailbox;
    this.gates = gates;
    this.campaigns = campaigns;
    this.fromDomain = fromDomain;
    this.drops = drops;
    this.ops = ops;
    this.activity = activity;
    this.locks = locks;
  }

  public int pollDue() {
    Instant now = Instant.now();
    List<MtsInboxEntity> dueBoxes = new ArrayList<>();
    for (MtsInboxEntity box : inboxes.findAll()) {
      if (box.isEnabled() && due(box, now)) {
        dueBoxes.add(box);
      }
    }
    if (dueBoxes.isEmpty()) {
      return 0;
    }
    activity.write(ActivityLogService.MTS, "START", "mts poll mailboxes=" + dueBoxes.size());
    int created = 0;
    try {
      for (MtsInboxEntity box : dueBoxes) {
        if (!tryStart(box.getId())) {
          continue;
        }
        created += pollOne(box.getId());
      }
      return created;
    } finally {
      activity.write(ActivityLogService.MTS, "END", "mts poll campaigns=" + created);
    }
  }

  public boolean startPollAsync(String id) {
    if (!tryStart(id)) {
      return false;
    }
    async.execute(() -> pollOne(id));
    return true;
  }

  @Transactional
  public void breakLock(String id) {
    MtsInboxEntity box =
        inboxes.findById(id).orElseThrow(() -> new IllegalArgumentException("not found"));
    box.setPolling(false);
    inboxes.save(box);
  }

  /** Removes SEEN messages left behind when delete-after-accept was off (test cleanup). */
  public int deleteSeenMessages(String id) {
    MtsInboxEntity box =
        inboxes.findById(id).orElseThrow(() -> new IllegalArgumentException("not found"));
    if (locks.stillHeld(box.isPolling(), box.getLastPollingUpdate(), Instant.now())) {
      throw new IllegalStateException("mailbox is polling; break lock or wait, then retry");
    }
    int n =
        mailbox.deleteSeen(
            new MailboxPort.MailboxConn(
                box.getHost(),
                box.getPort(),
                box.getUsername(),
                box.getPassword(),
                box.isUseSsl(),
                box.isUseTls(),
                "INBOX"));
    activity.write(
        ActivityLogService.MTS, "CLEANUP", mailboxRef(box) + " deletedSeen=" + n);
    ops.opsLog("mts.cleanup", "inbox=" + box.getName() + " deletedSeen=" + n);
    return n;
  }

  private boolean due(MtsInboxEntity box, Instant now) {
    if (box.getLastPolledAt() == null) {
      return true;
    }
    return box.getLastPolledAt()
        .plus(Math.max(1, box.getPollIntervalMinutes()), ChronoUnit.MINUTES)
        .isBefore(now);
  }

  private boolean tryStart(String id) {
    MtsInboxEntity box = inboxes.findById(id).orElse(null);
    if (box == null) {
      return false;
    }
    Instant now = Instant.now();
    if (locks.stillHeld(box.isPolling(), box.getLastPollingUpdate(), now)) {
      return false;
    }
    box.setPolling(true);
    box.setLastPollingUpdate(now);
    inboxes.save(box);
    return true;
  }

  private void heartbeat(MtsInboxEntity box) {
    box.setLastPollingUpdate(Instant.now());
    inboxes.save(box);
  }

  private void finish(MtsInboxEntity box, boolean ok) {
    Instant now = Instant.now();
    box.setPolling(false);
    box.setLastPollingUpdate(now);
    box.setLastPolledAt(now);
    box.setLastPollOk(ok);
    inboxes.save(box);
  }

  int pollOne(String id) {
    MtsInboxEntity box = inboxes.findById(id).orElse(null);
    if (box == null) {
      return 0;
    }
    AtomicInteger created = new AtomicInteger();
    heartbeat(box);
    activity.write(ActivityLogService.MTS, "CONNECT", mailboxRef(box));
    try {
      mailbox.poll(
          new MailboxPort.MailboxConn(
              box.getHost(),
              box.getPort(),
              box.getUsername(),
              box.getPassword(),
              box.isUseSsl(),
              box.isUseTls(),
              "INBOX"),
          raw -> {
            MtsGateService.Decision decision = gates.evaluate(box.getId(), box.getName(), raw);
            String sender = MtsEmails.extract(raw.from());
            heartbeat(box);
            activity.write(
                ActivityLogService.MTS,
                "FETCH",
                mailboxRef(box)
                    + " from="
                    + raw.from()
                    + " subject="
                    + raw.subject()
                    + " result="
                    + decision.result());
            if (decision.result() == MtsGateResult.ACCEPT) {
              String email =
                  sender == null ? "" : sender.trim().toLowerCase(Locale.ROOT);
              List<MtsSenderListGrantEntity> senderGrants =
                  grants.findByInboxIdAndSenderEmailOrderByPositionAsc(box.getId(), email);
              if (senderGrants.isEmpty()) {
                ops.opsLog(
                    "mts.ingest",
                    "authorized but no list grants inbox=" + box.getId() + " sender=" + email);
                return MailboxPort.HandleResult.SEEN;
              }
              MtsAuthorizedSenderEntity auth =
                  senders
                      .findByInboxIdAndEmail(box.getId(), email)
                      .orElse(null);
              if (auth == null) {
                ops.opsLog(
                    "mts.ingest",
                    "authorized sender row missing inbox=" + box.getId() + " sender=" + email);
                return MailboxPort.HandleResult.SEEN;
              }
              var issue =
                  fromDomain.validateOrOpenIssue(box, auth, senderGrants, raw.subject());
              if (issue != null) {
                // hold-back: leave SEEN so it is not re-polled, but do not delete
                return MailboxPort.HandleResult.SEEN;
              }
              MimeBodyParser.Parsed parts = MimeBodyParser.parse(raw.bytes());
              String html = parts.html();
              String text = parts.text();
              if (!StringUtils.hasText(html) && !StringUtils.hasText(text) && StringUtils.hasText(raw.subject())) {
                text = raw.subject();
                html = "<p>" + escape(raw.subject()) + "</p>";
              }
              var campaign =
                  campaigns.create(
                      raw.subject(),
                      html,
                      text,
                      senderGrants,
                      box.isAutoQueue(),
                      parts.attachments(),
                      auth.getSendingPathId());
              String listIds =
                  senderGrants.stream()
                      .map(MtsSenderListGrantEntity::getListId)
                      .reduce((a, b) -> a + "," + b)
                      .orElse("");
              activity.write(
                  ActivityLogService.MTS,
                  "CAMPAIGN",
                  mailboxRef(box)
                      + " id="
                      + campaign.getId()
                      + " lists="
                      + listIds
                      + " subject="
                      + campaign.getSubject()
                      + " attachments="
                      + parts.attachments().size());
              created.incrementAndGet();
              ops.opsLog(
                  "mts.accept", "inbox=" + box.getName() + " sender=" + email);
              return box.isDeleteAfterAccept()
                  ? MailboxPort.HandleResult.DELETE
                  : MailboxPort.HandleResult.SEEN;
            } else if (decision.result() == MtsGateResult.NOTIFY_CROSS_INBOX) {
              drops.record(box.getId(), sender, decision.reason(), false);
              drops.notifyCrossInbox(sender, box.getName());
            } else if (decision.result() == MtsGateResult.DROP_COUNTED
                || decision.result() == MtsGateResult.DROP_SILENT) {
              drops.record(
                  box.getId(), sender, decision.reason(), decision.countAutoBlock());
            }
            return MailboxPort.HandleResult.SEEN;
          });
      finish(box, true);
      activity.write(ActivityLogService.MTS, "DISCONNECT", mailboxRef(box));
      if (created.get() > 0) {
        ops.opsLog("mts.poll", "inbox=" + box.getName() + " campaigns=" + created.get());
      }
    } catch (MailboxQueryException ex) {
      finish(box, false);
      activity.write(
          ActivityLogService.MTS,
          "DISCONNECT",
          mailboxRef(box) + " error=" + ex.getMessage());
      ops.opsLog("mts.poll", "inbox=" + box.getName() + " error=" + ex.getMessage());
      ops.attentionAdmin("MTS inbox poll failed", box.getName() + ": " + ex.getMessage());
      log.warn("mts poll {}: {}", box.getName(), ex.getMessage());
    } catch (RuntimeException ex) {
      finish(box, false);
      throw ex;
    }
    return created.get();
  }

  /** Keep mailboxId= for per-box log filters; name is for operators. */
  static String mailboxRef(MtsInboxEntity box) {
    return "mailbox=" + box.getName() + " mailboxId=" + box.getId();
  }

  private static String escape(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
