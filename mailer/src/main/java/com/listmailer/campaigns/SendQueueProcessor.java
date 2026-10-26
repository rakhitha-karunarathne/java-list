package com.listmailer.campaigns;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * System send-queue drain. Same schedule class never overlaps in-process (no ShedLock). Concurrent
 * other @Scheduled classes may run.
 */
@Component
public class SendQueueProcessor {

  private static final Logger log = LoggerFactory.getLogger(SendQueueProcessor.class);

  private final CampaignSendService sendService;
  private final SendQueueRepository queue;
  private final ParametersPort parameters;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public SendQueueProcessor(
      CampaignSendService sendService, SendQueueRepository queue, ParametersPort parameters) {
    this.sendService = sendService;
    this.queue = queue;
    this.parameters = parameters;
  }

  @Scheduled(fixedDelayString = "${mailer.send-queue.tick-ms:60000}")
  public void tick() {
    if (!running.compareAndSet(false, true)) {
      return; // same schedule class non-overlap
    }
    try {
      int batch = positive(ParameterKeys.SEND_BATCH_SIZE, 600);
      int minutes = positive(ParameterKeys.SEND_BATCH_DURATION_MINUTES, 10);
      int perHour = positive(ParameterKeys.SEND_MESSAGES_PER_HOUR, 3600);
      long delayMs = nonNegative(ParameterKeys.SEND_DELAY_PER_EMAIL_MS, 1000);
      long sentInWindow =
          queue.countByStatusAndSentAtAfter("SENT", Instant.now().minus(60, ChronoUnit.MINUTES));
      int allowed = SendLimits.allowed(batch, perHour, sentInWindow);
      if (allowed <= 0) {
        return;
      }
      Instant deadline = Instant.now().plus(minutes, ChronoUnit.MINUTES);
      sendService.processPendingBatch(allowed, delayMs, deadline);
    } catch (RuntimeException ex) {
      log.warn("send-queue tick: {}", ex.getMessage());
    } finally {
      running.set(false);
    }
  }

  private int positive(String key, int fallback) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                int n = Integer.parseInt(v.trim());
                return n >= 1 ? n : fallback;
              } catch (NumberFormatException e) {
                return fallback;
              }
            })
        .orElse(fallback);
  }

  private long nonNegative(String key, long fallback) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                long n = Long.parseLong(v.trim());
                return n >= 0 ? n : fallback;
              } catch (NumberFormatException e) {
                return fallback;
              }
            })
        .orElse(fallback);
  }
}
