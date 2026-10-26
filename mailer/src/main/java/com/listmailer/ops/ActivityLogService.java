package com.listmailer.ops;

import com.listmailer.kernel.ids.Guids;
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

  public ActivityLogService(ActivityLogRepository logs) {
    this.logs = logs;
  }

  public void write(String kind, String event, String message) {
    logs.save(new ActivityLogEntity(Guids.newGuid(), kind, event, message, Instant.now()));
  }
}
