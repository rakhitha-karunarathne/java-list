package com.listmailer.bounce;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.ops.RetentionDays;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Purge old PROCESSED and UNCORRELATED bounce_events (Bounce Messages). */
@Component
public class BounceEventsRetentionJob {

  private final BounceEventRepository events;
  private final ParametersPort parameters;

  public BounceEventsRetentionJob(BounceEventRepository events, ParametersPort parameters) {
    this.events = events;
    this.parameters = parameters;
  }

  @Scheduled(cron = "${mailer.bounce.messages.retention-cron:0 15 4 * * *}")
  @Transactional
  public int purge() {
    int n = 0;
    n +=
        purgeStatus(
            "PROCESSED", ParameterKeys.BOUNCE_MESSAGES_PROCESSED_RETENTION_DAYS, 60);
    n +=
        purgeStatus(
            "UNCORRELATED", ParameterKeys.BOUNCE_MESSAGES_UNCORRELATED_RETENTION_DAYS, 60);
    return n;
  }

  private int purgeStatus(String status, String daysKey, int defaultDays) {
    int days = RetentionDays.read(parameters, daysKey, defaultDays);
    Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
    return events.deleteByStatusAndReceivedAtBefore(status, cutoff);
  }
}
