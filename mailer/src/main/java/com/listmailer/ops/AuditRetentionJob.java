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
    int days = intParam(ParameterKeys.AUDIT_RETENTION_DAYS, 365);
    Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
    return audit.deleteOlderThan(cutoff);
  }

  private int intParam(String key, int def) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                int x = Integer.parseInt(v.trim());
                return x > 0 ? x : def;
              } catch (NumberFormatException e) {
                return def;
              }
            })
        .orElse(def);
  }
}
