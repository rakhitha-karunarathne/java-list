package com.listmailer.ops;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.parameters.ParameterEntity;
import com.listmailer.parameters.ParameterRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ObservabilityE2ETest {

  @Container
  @ServiceConnection
  static MariaDBContainer<?> maria =
      new MariaDBContainer<>("mariadb:12.3.3")
          .withDatabaseName("mailer")
          .withUsername("mailer")
          .withPassword("mailer");

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("mailer.public-base-url", () -> "https://lists.example.com");
    registry.add("mailer.send-queue.tick-ms", () -> "3600000");
    registry.add("mailer.mts.poll-tick-ms", () -> "3600000");
    registry.add("mailer.bounce.poll-tick-ms", () -> "3600000");
    registry.add("mailer.bounce.correlate-tick-ms", () -> "3600000");
    registry.add("mailer.jwt.secret", () -> "dev-only-change-me-32chars-minimum!!");
    registry.add("mailer.opaque-token.secret", () -> "dev-only-opaque-token-32chars-min!!");
    registry.add("mailer.bootstrap.email", () -> "admin@example.com");
    registry.add("mailer.bootstrap.password", () -> "changeme");
  }

  @Autowired PersistingOpsHooks hooks;
  @Autowired OpsLogRepository opsLogs;
  @Autowired AttentionItemRepository attention;
  @Autowired AuditLogRepository audit;
  @Autowired OpsRetentionJob opsRetention;
  @Autowired AuditRetentionJob auditRetention;
  @Autowired ParameterRepository parameters;

  @Test
  @DisplayName("R2/R1 ops failure → Ops Logs + Admin Attention")
  void opsFailureToAttention() {
    hooks.opsLog("send.test", "test send failed to x@y.com: refused");
    hooks.attentionAdmin("Test send failed", "Campaign c-e2e → x@y.com");

    assertTrue(
        opsLogs.findAllByOrderByCreatedAtDesc().stream()
            .anyMatch(o -> o.getCategory().equals("send.test") && o.getSeverity().equals("ERROR")),
        "R2: send failure missing from Ops Logs");
    assertTrue(
        attention.findByOpenTrueOrderByCreatedAtDesc().stream()
            .anyMatch(a -> "OPS_ERROR".equals(a.getKind())),
        "R1: Admin Attention ops highlight missing");
  }

  @Test
  @DisplayName("R2 bounce and MTS poll ops rows")
  void bounceAndMtsPollRows() {
    hooks.opsLog("bounce.poll", "mailbox=bounce-e2e ingested=0");
    hooks.opsLog("mts.poll", "inbox=mts-e2e campaigns=0");
    assertTrue(
        opsLogs.findByCategoryOrderByCreatedAtDesc("bounce.poll").stream().findFirst().isPresent(),
        "R2: bounce poll ops row");
    assertTrue(
        opsLogs.findByCategoryOrderByCreatedAtDesc("mts.poll").stream().findFirst().isPresent(),
        "R2: MTS poll ops row");
  }

  @Test
  @DisplayName("R3 Campaign status Audit + no secrets")
  void campaignStatusAudit() {
    hooks.audit("campaign.queue", "campaign=c1", "admin@example.com", null, null);
    hooks.audit("campaign.stop", "campaign=c1", "admin@example.com", null, null);
    hooks.audit("campaign.requeue", "campaign=c1", "admin@example.com", null, null);
    hooks.audit("campaign.finished", "campaign=c1", "system", null, null);
    hooks.audit("campaign.list_added", "campaign=c1 list=L2", "admin@example.com", "L2", null);
    hooks.audit("unsubscribe", "member=m1 campaign=c1 lists=1", null, null, "m1");
    hooks.audit("bounce.disable", "member=m1 class=HARD", null, null, "m1");

    var actions = audit.findAllByOrderByCreatedAtDesc().stream().map(AuditLogEntity::getAction).toList();
    assertTrue(actions.contains("campaign.queue"), "R3: Queue");
    assertTrue(actions.contains("campaign.stop"), "R3: Stop");
    assertTrue(actions.contains("campaign.requeue"), "R3: Requeue");
    assertTrue(actions.contains("campaign.finished"), "R3: Finished");
    assertTrue(actions.contains("campaign.list_added"), "R3: list added");
    assertTrue(actions.contains("unsubscribe"), "R3: unsub sample");
    assertTrue(actions.contains("bounce.disable"), "R3: bounce sample");
    assertFalse(
        audit.findAll().stream().anyMatch(a -> a.getDetail().toLowerCase().contains("password=")),
        "C1: secret in Audit");
  }

  @Test
  @DisplayName("R2/R3 retention purge honors Parameters")
  void retentionPurge() {
    parameters.save(new ParameterEntity(ParameterKeys.OPS_LOG_RETENTION_DAYS, "1", "test"));
    parameters.save(new ParameterEntity(ParameterKeys.AUDIT_RETENTION_DAYS, "1", "test"));

    String oldOps = Guids.newGuid();
    opsLogs.save(
        new OpsLogEntity(oldOps, "send.test", "ERROR", "old", Instant.now().minus(3, ChronoUnit.DAYS)));
    String keepOps = Guids.newGuid();
    opsLogs.save(new OpsLogEntity(keepOps, "send.test", "INFO", "new", Instant.now()));

    String oldAudit = Guids.newGuid();
    audit.save(
        new AuditLogEntity(
            oldAudit,
            "campaign.stop",
            "old",
            "a",
            null,
            null,
            Instant.now().minus(3, ChronoUnit.DAYS)));
    String keepAudit = Guids.newGuid();
    audit.save(
        new AuditLogEntity(keepAudit, "campaign.queue", "new", "a", null, null, Instant.now()));

    opsRetention.purge();
    auditRetention.purge();

    assertFalse(opsLogs.findById(oldOps).isPresent(), "R2: expired ops purged");
    assertTrue(opsLogs.findById(keepOps).isPresent(), "R2: in-window ops kept");
    assertFalse(audit.findById(oldAudit).isPresent(), "R3: expired audit purged");
    assertTrue(audit.findById(keepAudit).isPresent(), "R3: in-window audit kept");
  }
}
