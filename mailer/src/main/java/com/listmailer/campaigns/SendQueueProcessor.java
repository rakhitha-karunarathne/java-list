package com.listmailer.campaigns;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
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
  private final ParametersPort parameters;
  private final AtomicBoolean running = new AtomicBoolean(false);

  public SendQueueProcessor(CampaignSendService sendService, ParametersPort parameters) {
    this.sendService = sendService;
    this.parameters = parameters;
  }

  @Scheduled(fixedDelayString = "${mailer.send-queue.tick-ms:60000}")
  public void tick() {
    if (!running.compareAndSet(false, true)) {
      return; // same schedule class non-overlap
    }
    try {
      int perMinute = throttlePerMinute();
      int batch = perMinute <= 0 ? 500 : Math.max(1, perMinute / 30);
      int sent = sendService.processPendingBatch(batch);
      if (sent > 0 && perMinute > 0) {
        long sleepMs = Math.max(0, (60_000L * sent) / perMinute);
        if (sleepMs > 0) {
          Thread.sleep(Math.min(sleepMs, 5_000L));
        }
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    } catch (RuntimeException ex) {
      log.warn("send-queue tick: {}", ex.getMessage());
    } finally {
      running.set(false);
    }
  }

  private int throttlePerMinute() {
    return parameters
        .get(ParameterKeys.SEND_THROTTLE_PER_MINUTE)
        .map(
            v -> {
              try {
                return Integer.parseInt(v.trim());
              } catch (NumberFormatException e) {
                return 120;
              }
            })
        .orElse(120);
  }
}
