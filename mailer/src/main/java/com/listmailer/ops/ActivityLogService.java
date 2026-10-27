package com.listmailer.ops;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ActivityLogService {

  public static final String BOUNCE = "BOUNCE";
  public static final String SEND = "SEND";
  public static final String MTS = "MTS";
  public static final Set<String> KINDS = Set.of(BOUNCE, SEND, MTS);

  private final ActivityLogRepository logs;
  private final ParametersPort parameters;

  public ActivityLogService(ActivityLogRepository logs, ParametersPort parameters) {
    this.logs = logs;
    this.parameters = parameters;
  }

  public void write(String kind, String event, String message) {
    if (!enabled(kind)) {
      return;
    }
    logs.save(new ActivityLogEntity(Guids.newGuid(), kind, event, message, Instant.now()));
  }

  private boolean enabled(String kind) {
    String key =
        switch (kind) {
          case BOUNCE -> ParameterKeys.BOUNCE_PROCESS_LOG_ENABLED;
          case SEND -> ParameterKeys.SEND_QUEUE_LOG_ENABLED;
          case MTS -> ParameterKeys.MTS_PROCESS_LOG_ENABLED;
          default -> null;
        };
    if (key == null) {
      return false;
    }
    return parameters.get(key).map(v -> !"false".equalsIgnoreCase(v.trim())).orElse(true);
  }
}
