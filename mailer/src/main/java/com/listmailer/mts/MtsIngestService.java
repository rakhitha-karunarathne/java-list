package com.listmailer.mts;

import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.mailbox.MailboxQueryException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
  private final MtsMappingRepository mappings;
  private final MtsMappingListRepository mappingLists;
  private final MailboxPort mailbox;
  private final MtsGateService gates;
  private final MtsCampaignFactory campaigns;
  private final MtsFromDomainService fromDomain;
  private final MtsDropService drops;
  private final OpsHooksPort ops;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public MtsIngestService(
      MtsInboxRepository inboxes,
      MtsMappingRepository mappings,
      MtsMappingListRepository mappingLists,
      MailboxPort mailbox,
      MtsGateService gates,
      MtsCampaignFactory campaigns,
      MtsFromDomainService fromDomain,
      MtsDropService drops,
      OpsHooksPort ops) {
    this.inboxes = inboxes;
    this.mappings = mappings;
    this.mappingLists = mappingLists;
    this.mailbox = mailbox;
    this.gates = gates;
    this.campaigns = campaigns;
    this.fromDomain = fromDomain;
    this.drops = drops;
    this.ops = ops;
  }

  public int pollDue() {
    if (!running.compareAndSet(false, true)) {
      return 0;
    }
    try {
      int created = 0;
      Instant now = Instant.now();
      for (MtsInboxEntity box : inboxes.findAll()) {
        if (!box.isEnabled()) {
          continue;
        }
        if (!due(box, now)) {
          continue;
        }
        created += pollOne(box, now);
      }
      return created;
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
    List<MtsMappingEntity> maps = mappings.findByInboxId(box.getId());
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
            String sender = MtsGateService.extractEmail(raw.from());
            if (decision.result() == MtsGateResult.ACCEPT) {
              if (maps.isEmpty()) {
                ops.opsLog("mts.ingest", "authorized but no mapping inbox=" + box.getId());
                return true;
              }
              MtsMappingEntity map = maps.get(0);
              List<String> listIds =
                  mappingLists.findByMappingIdOrderByPositionAsc(map.getId()).stream()
                      .map(MtsMappingListEntity::getListId)
                      .toList();
              if (listIds.isEmpty()) {
                return true;
              }
              var issue =
                  fromDomain.validateOrOpenIssue(box, map, listIds, sender, raw.subject());
              if (issue != null) {
                return true;
              }
              String html = extractHtml(raw);
              String text = extractText(raw);
              campaigns.createQueued(raw.subject(), html, text, map, listIds);
              created.incrementAndGet();
              ops.opsLog("mts.accept", "inbox=" + box.getName() + " mapping=" + map.getId());
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
      if (created.get() > 0) {
        ops.opsLog("mts.poll", "inbox=" + box.getName() + " campaigns=" + created.get());
      }
    } catch (MailboxQueryException ex) {
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
