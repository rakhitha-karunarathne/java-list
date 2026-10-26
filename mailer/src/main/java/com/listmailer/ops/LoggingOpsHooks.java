package com.listmailer.ops;

import com.listmailer.kernel.ports.OpsHooksPort;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** In-memory + log sink until epic-ops-attention-audit persists Ops/Attention. */
@Component
public class LoggingOpsHooks implements OpsHooksPort {

  private static final Logger log = LoggerFactory.getLogger(LoggingOpsHooks.class);

  private final List<String> opsEntries = Collections.synchronizedList(new ArrayList<>());
  private final List<String> attentionEntries = Collections.synchronizedList(new ArrayList<>());
  private final List<String> auditEntries = Collections.synchronizedList(new ArrayList<>());

  private static final int CAP = 500;

  @Override
  public void opsLog(String category, String message) {
    String line = category + ": " + message;
    opsEntries.add(line);
    trim(opsEntries);
    log.error("OPS {}", line);
  }

  @Override
  public void attentionAdmin(String title, String detail) {
    String line = title + " — " + detail;
    attentionEntries.add(line);
    trim(attentionEntries);
    log.warn("ATTENTION {}", line);
  }

  @Override
  public void audit(String action, String detail) {
    String line = action + ": " + detail;
    auditEntries.add(line);
    trim(auditEntries);
    log.info("AUDIT {}", line);
  }

  private static void trim(List<String> list) {
    while (list.size() > CAP) {
      list.remove(0);
    }
  }

  /** Test/support peek; not a public product API. */
  public List<String> opsSnapshot() {
    return List.copyOf(opsEntries);
  }

  public List<String> attentionSnapshot() {
    return List.copyOf(attentionEntries);
  }

  public List<String> auditSnapshot() {
    return List.copyOf(auditEntries);
  }
}
