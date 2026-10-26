package com.listmailer.ops;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuditRetentionJob {

  private final AuditLogRepository audit;
  private final ParametersPort parameters;

  public AuditRetentionJob(AuditLogRepository audit, ParametersPort parameters) {
    this.audit = audit;
    this.parameters = parameters;
  }

  @Scheduled(cron = "${mailer.audit.retention-cron:0 45 3 * * *}")
  @Transactional
  public int purge() {
    int days = RetentionDays.read(parameters, ParameterKeys.AUDIT_RETENTION_DAYS, 365);
    Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
    return audit.deleteOlderThan(cutoff);
  }
}
