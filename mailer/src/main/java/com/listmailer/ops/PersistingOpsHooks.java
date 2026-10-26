package com.listmailer.ops;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.OpsHooksPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Durable Ops Logs + Admin Attention; Audit persistence lands in story 9.3. */
@Component
@Primary
public class PersistingOpsHooks implements OpsHooksPort {

  private static final Logger log = LoggerFactory.getLogger(PersistingOpsHooks.class);

  private final OpsLogRepository opsLogs;
  private final AttentionItemRepository attention;
  private final AuditLogRepository auditLogs;

  private final List<String> auditEntries = Collections.synchronizedList(new ArrayList<>());

  public PersistingOpsHooks(
      OpsLogRepository opsLogs, AttentionItemRepository attention, AuditLogRepository auditLogs) {
    this.opsLogs = opsLogs;
    this.attention = attention;
    this.auditLogs = auditLogs;
  }

  @Override
  @Transactional
  public void opsLog(String category, String message) {
    String cat = category == null ? "ops" : category;
    String sev = cat.contains("error") || message != null && message.contains("error=") ? "ERROR" : "INFO";
    if (cat.startsWith("send.") && message != null && message.contains("fail")) {
      sev = "ERROR";
    }
    if (cat.contains("poll") && message != null && message.contains("error")) {
      sev = "ERROR";
    }
    opsLogs.save(new OpsLogEntity(Guids.newGuid(), cat, sev, safe(message), Instant.now()));
    log.info("OPS {}: {}", cat, safe(message));
  }

  @Override
  @Transactional
  public void attentionAdmin(String title, String detail) {
    attention.save(
        new AttentionItemEntity(
            Guids.newGuid(), title, safe(detail), "OPS_ERROR", null, Instant.now()));
    log.warn("ATTENTION {}: {}", title, safe(detail));
  }

  @Override
  @Transactional
  public void audit(String action, String detail, String actor, String listId, String memberId) {
    String safeDetail = safe(detail);
    String line = action + ": " + safeDetail;
    auditEntries.add(line);
    while (auditEntries.size() > 500) {
      auditEntries.remove(0);
    }
    auditLogs.save(
        new AuditLogEntity(
            Guids.newGuid(), action, safeDetail, actor, listId, memberId, Instant.now()));
    log.info("AUDIT {}", line);
  }

  @Override
  public void audit(String action, String detail) {
    audit(action, detail, null, null, null);
  }

  public List<String> opsSnapshot() {
    return opsLogs.findAllByOrderByCreatedAtDesc().stream()
        .map(o -> o.getCategory() + ": " + o.getMessage())
        .toList();
  }

  public List<String> attentionSnapshot() {
    return attention.findByOpenTrueOrderByCreatedAtDesc().stream()
        .map(a -> a.getTitle() + " — " + a.getDetail())
        .toList();
  }

  public List<String> auditSnapshot() {
    return List.copyOf(auditEntries);
  }

  /** Strip credential-ish substrings from persisted text (C1). */
  static String safe(String message) {
    if (message == null) {
      return "";
    }
    return message
        .replaceAll("(?i)password[=:]\\S+", "password=***")
        .replaceAll("(?i)secret[=:]\\S+", "secret=***")
        .replaceAll("(?i)token[=:]\\S+", "token=***");
  }
}
