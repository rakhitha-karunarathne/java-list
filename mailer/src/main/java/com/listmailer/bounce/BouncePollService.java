package com.listmailer.bounce;

import com.listmailer.catalogs.BounceMailboxEntity;
import com.listmailer.catalogs.BounceMailboxRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.mailbox.MailboxQueryException;
import com.listmailer.ops.ActivityLogService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BouncePollService {

  private static final Logger log = LoggerFactory.getLogger(BouncePollService.class);

  private final BounceMailboxRepository mailboxes;
  private final BounceEventRepository events;
  private final MailboxPort mailbox;
  private final OpsHooksPort ops;
  private final ActivityLogService activity;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public BouncePollService(
      BounceMailboxRepository mailboxes,
      BounceEventRepository events,
      MailboxPort mailbox,
      OpsHooksPort ops,
      ActivityLogService activity) {
    this.mailboxes = mailboxes;
    this.events = events;
    this.mailbox = mailbox;
    this.ops = ops;
    this.activity = activity;
  }

  /** Sequential poll of due bounce mailboxes. Same schedule class non-overlap. */
  public int pollDue() {
    if (!running.compareAndSet(false, true)) {
      return 0;
    }
    try {
      Instant now = Instant.now();
      List<BounceMailboxEntity> dueBoxes = new ArrayList<>();
      for (BounceMailboxEntity box : mailboxes.findAll()) {
        if (box.isEnabled() && due(box, now)) {
          dueBoxes.add(box);
        }
      }
      // shortcut: idle ticks (nothing due) are not written; log when a mailbox is polled
      if (dueBoxes.isEmpty()) {
        return 0;
      }
      activity.write(ActivityLogService.BOUNCE, "START", "bounce poll mailboxes=" + dueBoxes.size());
      int total = 0;
      try {
        for (BounceMailboxEntity box : dueBoxes) {
          total += pollOne(box, now);
        }
        return total;
      } finally {
        activity.write(ActivityLogService.BOUNCE, "END", "bounce poll ingested=" + total);
      }
    } finally {
      running.set(false);
    }
  }

  private boolean due(BounceMailboxEntity box, Instant now) {
    if (box.getLastPolledAt() == null) {
      return true;
    }
    return box.getLastPolledAt()
        .plus(Math.max(1, box.getPollIntervalMinutes()), ChronoUnit.MINUTES)
        .isBefore(now);
  }

  int pollOne(BounceMailboxEntity box, Instant now) {
    AtomicInteger count = new AtomicInteger();
    activity.write(ActivityLogService.BOUNCE, "CONNECT", "inbox=" + box.getName());
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
            String headersJson = headersToText(raw.headers());
            events.save(
                new BounceEventEntity(
                    Guids.newGuid(),
                    box.getId(),
                    now,
                    raw.messageId(),
                    raw.subject(),
                    raw.from(),
                    headersJson,
                    raw.bytes(),
                    "INGESTED"));
            count.incrementAndGet();
            activity.write(
                ActivityLogService.BOUNCE,
                "FETCH",
                "inbox="
                    + box.getName()
                    + " from="
                    + raw.from()
                    + " subject="
                    + raw.subject()
                    + " messageId="
                    + raw.messageId());
            return true;
          });
      box.setLastPolledAt(now);
      mailboxes.save(box);
      activity.write(ActivityLogService.BOUNCE, "DISCONNECT", "inbox=" + box.getName());
      if (count.get() > 0) {
        ops.opsLog(
            "bounce.poll",
            "mailbox=" + box.getName() + " id=" + box.getId() + " ingested=" + count.get());
      }
    } catch (MailboxQueryException ex) {
      activity.write(
          ActivityLogService.BOUNCE,
          "DISCONNECT",
          "inbox=" + box.getName() + " error=" + ex.getMessage());
      ops.opsLog("bounce.poll", "mailbox=" + box.getName() + " error=" + ex.getMessage());
      ops.attentionAdmin("Bounce mailbox poll failed", box.getName() + ": " + ex.getMessage());
      log.warn("bounce poll {}: {}", box.getName(), ex.getMessage());
      box.setLastPolledAt(now);
      mailboxes.save(box);
    }
    return count.get();
  }

  boolean isRunning() {
    return running.get();
  }

  List<BounceEventEntity> ingested() {
    return events.findByStatus("INGESTED");
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
