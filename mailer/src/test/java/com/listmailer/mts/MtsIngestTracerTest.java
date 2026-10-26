package com.listmailer.mts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.catalogs.SendingPathEntity;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
class MtsIngestTracerTest {

  @Container
  @ServiceConnection
  static MariaDBContainer<?> maria =
      new MariaDBContainer<>("mariadb:12.3.3")
          .withDatabaseName("mailer")
          .withUsername("mailer")
          .withPassword("mailer");

  @RegisterExtension
  static GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetup.dynamicPort(ServerSetupTest.SMTP_IMAP))
          .withConfiguration(
              GreenMailConfiguration.aConfig()
                  .withUser("mts@localhost", "mts")
                  .withUser("mailer", "mailer"))
          .withPerMethodLifecycle(true);

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

  @Autowired MtsIngestService ingest;
  @Autowired MtsInboxRepository inboxes;
  @Autowired MtsAuthorizedSenderRepository senders;
  @Autowired MtsMappingRepository mappings;
  @Autowired MtsMappingListRepository mappingLists;
  @Autowired CampaignRepository campaigns;
  @Autowired SendingPathRepository paths;
  @Autowired ListRepository lists;
  @Autowired MemberRepository members;
  @Autowired ListMemberRepository listMembers;

  private String pathId;
  private String listId;

  @BeforeEach
  void seed() {
    pathId = UUID.randomUUID().toString();
    paths.save(
        new SendingPathEntity(
            pathId,
            "p-" + pathId.substring(0, 8),
            "127.0.0.1",
            greenMail.getSmtp().getPort(),
            "mailer",
            "mailer",
            "from@localhost",
            "Mailer",
            false,
            Instant.now()));
    listId = UUID.randomUUID().toString();
    lists.save(
        new ListEntity(listId, "L-" + listId.substring(0, 8), pathId, null, null, null, null, Instant.now()));
    String mid = UUID.randomUUID().toString();
    members.save(new MemberEntity(mid, "member@example.com", true, Instant.now()));
    listMembers.save(new ListMemberEntity(listId, mid, false, Instant.now()));
  }

  @Test
  @DisplayName("R1/R7 authorized IMAP message becomes Queued Campaign")
  void authorizedBecomesQueued() {
    String inboxId = UUID.randomUUID().toString();
    inboxes.save(
        new MtsInboxEntity(
            inboxId,
            "mts-" + inboxId.substring(0, 6),
            "127.0.0.1",
            greenMail.getImap().getPort(),
            "mts@localhost",
            "mts",
            false,
            1,
            Instant.now()));
    senders.save(new MtsAuthorizedSenderEntity(inboxId, "author@example.com"));
    String mapId = UUID.randomUUID().toString();
    mappings.save(new MtsMappingEntity(mapId, inboxId, "default", null, null, Instant.now()));
    mappingLists.save(new MtsMappingListEntity(mapId, listId, 0));

    GreenMailUtil.sendTextEmail(
        "mts@localhost",
        "author@example.com",
        "Hello MTS",
        "Body from authorized",
        greenMail.getSmtp().getServerSetup());
    assertTrue(greenMail.waitForIncomingEmail(15_000, 1));

    int n = ingest.pollDue();
    assertEquals(1, n);
    assertTrue(
        campaigns.findAll().stream().anyMatch(c -> c.getStatus() == CampaignStatus.QUEUED));
    assertTrue(
        campaigns.findAll().stream().anyMatch(c -> "Hello MTS".equals(c.getSubject())));
  }
}
