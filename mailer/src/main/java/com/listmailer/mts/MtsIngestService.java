package com.listmailer.mts;

import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.mailbox.MailboxQueryException;
import com.listmailer.ops.ActivityLogService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MtsIngestService {

  private static final Logger log = LoggerFactory.getLogger(MtsIngestService.class);

  private final MtsInboxRepository inboxes;
  private final MtsSenderListGrantRepository grants;
  private final MailboxPort mailbox;
  private final MtsGateService gates;
  private final MtsCampaignFactory campaigns;
  private final MtsFromDomainService fromDomain;
  private final MtsDropService drops;
  private final OpsHooksPort ops;
  private final ActivityLogService activity;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public MtsIngestService(
      MtsInboxRepository inboxes,
      MtsSenderListGrantRepository grants,
      MailboxPort mailbox,
      MtsGateService gates,
      MtsCampaignFactory campaigns,
      MtsFromDomainService fromDomain,
      MtsDropService drops,
      OpsHooksPort ops,
      ActivityLogService activity) {
    this.inboxes = inboxes;
    this.grants = grants;
    this.mailbox = mailbox;
    this.gates = gates;
    this.campaigns = campaigns;
    this.fromDomain = fromDomain;
    this.drops = drops;
    this.ops = ops;
    this.activity = activity;
  }

  public int pollDue() {
    if (!running.compareAndSet(false, true)) {
      return 0;
    }
    try {
      Instant now = Instant.now();
      List<MtsInboxEntity> dueBoxes = new ArrayList<>();
      for (MtsInboxEntity box : inboxes.findAll()) {
        if (box.isEnabled() && due(box, now)) {
          dueBoxes.add(box);
        }
      }
      // shortcut: idle ticks are not written
      if (dueBoxes.isEmpty()) {
        return 0;
      }
      activity.write(ActivityLogService.MTS, "START", "mts poll mailboxes=" + dueBoxes.size());
      int created = 0;
      try {
        for (MtsInboxEntity box : dueBoxes) {
          created += pollOne(box, now);
        }
        return created;
      } finally {
        activity.write(ActivityLogService.MTS, "END", "mts poll campaigns=" + created);
      }
    } finally {
      running.set(false);
    }
  }

  private boolean due(MtsInboxEntity box, Instant now) {
    if (box.getLastPolledAt() == null) {
      return true;
    }
    return box.getLastPolledAt()
        .plus(Math.max(1, box.getPollIntervalMinutes()), ChronoUnit.MINUTES)
        .isBefore(now);
  }

  int pollOne(MtsInboxEntity box, Instant now) {
    AtomicInteger created = new AtomicInteger();
    activity.write(ActivityLogService.MTS, "CONNECT", "inbox=" + box.getName());
    try {
      mailbox.poll(
          new MailboxPort.MailboxConn(
              box.getHost(),
              box.getPort(),
              box.getUsername(),
              box.getPassword(),
              box.isUseSsl(),
              "INBOX"),
          raw -> {
            MtsGateService.Decision decision = gates.evaluate(box.getId(), box.getName(), raw);
            String sender = MtsEmails.extract(raw.from());
            activity.write(
                ActivityLogService.MTS,
                "FETCH",
                "inbox="
                    + box.getName()
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
                return true;
              }
              var issue =
                  fromDomain.validateOrOpenIssue(box, senderGrants, email, raw.subject());
              if (issue != null) {
                return true;
              }
              String html = extractHtml(raw);
              String text = extractText(raw);
              var campaign = campaigns.createQueued(raw.subject(), html, text, senderGrants);
              String listIds =
                  senderGrants.stream()
                      .map(MtsSenderListGrantEntity::getListId)
                      .reduce((a, b) -> a + "," + b)
                      .orElse("");
              activity.write(
                  ActivityLogService.MTS,
                  "CAMPAIGN",
                  "id="
                      + campaign.getId()
                      + " lists="
                      + listIds
                      + " subject="
                      + campaign.getSubject());
              created.incrementAndGet();
              ops.opsLog(
                  "mts.accept", "inbox=" + box.getName() + " sender=" + email);
            } else if (decision.result() == MtsGateResult.NOTIFY_CROSS_INBOX) {
              drops.record(box.getId(), sender, decision.reason(), false);
              drops.notifyCrossInbox(sender, box.getName());
            } else if (decision.result() == MtsGateResult.DROP_COUNTED
                || decision.result() == MtsGateResult.DROP_SILENT) {
              drops.record(
                  box.getId(), sender, decision.reason(), decision.countAutoBlock());
            }
            return true;
          });
      box.setLastPolledAt(now);
      inboxes.save(box);
      activity.write(ActivityLogService.MTS, "DISCONNECT", "inbox=" + box.getName());
      if (created.get() > 0) {
        ops.opsLog("mts.poll", "inbox=" + box.getName() + " campaigns=" + created.get());
      }
    } catch (MailboxQueryException ex) {
      activity.write(
          ActivityLogService.MTS, "DISCONNECT", "inbox=" + box.getName() + " error=" + ex.getMessage());
      ops.opsLog("mts.poll", "inbox=" + box.getName() + " error=" + ex.getMessage());
      ops.attentionAdmin("MTS inbox poll failed", box.getName() + ": " + ex.getMessage());
      log.warn("mts poll {}: {}", box.getName(), ex.getMessage());
      box.setLastPolledAt(now);
      inboxes.save(box);
    }
    return created.get();
  }

  static String extractHtml(MailboxPort.RawMessage raw) {
    return "<p>" + escape(bodyContent(raw)) + "</p>";
  }

  static String extractText(MailboxPort.RawMessage raw) {
    return bodyContent(raw);
  }

  private static String bodyContent(MailboxPort.RawMessage raw) {
    String body = new String(raw.bytes(), StandardCharsets.UTF_8);
    int sep = body.indexOf("\r\n\r\n");
    if (sep < 0) {
      sep = body.indexOf("\n\n");
    }
    String content = sep >= 0 ? body.substring(sep).trim() : body.trim();
    if (!StringUtils.hasText(content) && StringUtils.hasText(raw.subject())) {
      return raw.subject();
    }
    return content;
  }

  private static String escape(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
