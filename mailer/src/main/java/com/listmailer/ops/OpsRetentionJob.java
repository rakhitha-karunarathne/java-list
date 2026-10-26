package com.listmailer.ops;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OpsRetentionJob {

  private final OpsLogRepository opsLogs;
  private final ParametersPort parameters;

  public OpsRetentionJob(OpsLogRepository opsLogs, ParametersPort parameters) {
    this.opsLogs = opsLogs;
    this.parameters = parameters;
  }

  @Scheduled(cron = "${mailer.ops.retention-cron:0 30 3 * * *}")
  @Transactional
  public int purge() {
    int days = intParam(ParameterKeys.OPS_LOG_RETENTION_DAYS, 90);
    Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
    return opsLogs.deleteOlderThan(cutoff);
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
