package com.listmailer.ops;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ActivityLogRetentionJob {

  private final ActivityLogRepository logs;
  private final ParametersPort parameters;

  public ActivityLogRetentionJob(ActivityLogRepository logs, ParametersPort parameters) {
    this.logs = logs;
    this.parameters = parameters;
  }

  @Scheduled(cron = "${mailer.ops.retention-cron:0 30 3 * * *}")
  @Transactional
  public int purge() {
    int n = 0;
    n +=
        purgeKind(
            ActivityLogService.BOUNCE,
            ParameterKeys.BOUNCE_PROCESS_LOG_RETENTION_DAYS,
            ParameterKeys.BOUNCE_PROCESS_LOG_MAX_ROWS);
    n +=
        purgeKind(
            ActivityLogService.SEND,
            ParameterKeys.SEND_QUEUE_LOG_RETENTION_DAYS,
            ParameterKeys.SEND_QUEUE_LOG_MAX_ROWS);
    n +=
        purgeKind(
            ActivityLogService.MTS,
            ParameterKeys.MTS_PROCESS_LOG_RETENTION_DAYS,
            ParameterKeys.MTS_PROCESS_LOG_MAX_ROWS);
    return n;
  }

  private int purgeKind(String kind, String daysKey, String maxKey) {
    int days = RetentionDays.read(parameters, daysKey, 30);
    Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
    int n = logs.deleteKindOlderThan(kind, cutoff);
    int keep = RetentionDays.read(parameters, maxKey, 5000);
    n += logs.deleteKindBeyondNewest(kind, keep);
    return n;
  }
}
