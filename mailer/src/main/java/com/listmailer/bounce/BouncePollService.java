package com.listmailer.bounce;

import com.listmailer.catalogs.BounceMailboxEntity;
import com.listmailer.catalogs.BounceMailboxRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.mailbox.MailboxPollLock;
import com.listmailer.mailbox.MailboxQueryException;
import com.listmailer.ops.ActivityLogService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
  private final ExecutorService async = Executors.newCachedThreadPool();

  public BouncePollService(
      BounceMailboxRepository mailboxes,
      BounceEventRepository events,
      MailboxPort mailbox,
      OpsHooksPort ops,
      ActivityLogService activity,
      MailboxPollLock locks) {
    this.mailboxes = mailboxes;
    this.events = events;
    this.mailbox = mailbox;
    this.ops = ops;
    this.activity = activity;
    this.locks = locks;
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
    int total = 0;
    try {
      for (BounceMailboxEntity box : dueBoxes) {
        if (!tryStart(box.getId())) {
          continue;
        }
        total += pollOne(box.getId());
      }
      return total;
    } finally {
      activity.write(ActivityLogService.BOUNCE, "END", "bounce poll ingested=" + total);
    }
  }

  /** Start a poll of one mailbox on a background thread. Returns false if lock was already held. */
  public boolean startPollAsync(String id) {
    if (!tryStart(id)) {
      return false;
    }
    async.execute(() -> pollOne(id));
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

  int pollOne(String id) {
    BounceMailboxEntity box = mailboxes.findById(id).orElse(null);
    if (box == null) {
      return 0;
    }
    Instant now = Instant.now();
    AtomicInteger count = new AtomicInteger();
    heartbeat(box);
    activity.write(ActivityLogService.BOUNCE, "CONNECT", mailboxRef(box));
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
    return count.get();
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
