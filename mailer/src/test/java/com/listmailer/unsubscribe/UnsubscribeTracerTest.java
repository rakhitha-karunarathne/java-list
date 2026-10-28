package com.listmailer.unsubscribe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignSendService;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.catalogs.SendingPathEntity;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.lists.ListEntity;
import com.listmailer.secrets.SecretStore;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import com.listmailer.ops.PersistingOpsHooks;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Story 6.1 GreenMail tracer: send → click unsub → multi-list membership. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class UnsubscribeTracerTest {

  private static final Pattern UNSUB = Pattern.compile("/unsubscribe/([A-Za-z0-9_-]+)");

  @Container
  @ServiceConnection
  static MariaDBContainer<?> maria =
      new MariaDBContainer<>("mariadb:12.3.3")
          .withDatabaseName("mailer")
          .withUsername("mailer")
          .withPassword("mailer");

  @RegisterExtension
  static GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetupTest.SMTP.dynamicPort())
          .withConfiguration(GreenMailConfiguration.aConfig().withUser("mailer", "mailer"))
          .withPerMethodLifecycle(true);

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("mailer.public-base-url", () -> "https://lists.example.com");
    registry.add("mailer.send-queue.tick-ms", () -> "3600000");
    registry.add("mailer.jwt.secret", () -> "dev-only-change-me-32chars-minimum!!");
    registry.add("mailer.opaque-token.secret", () -> "dev-only-opaque-token-32chars-min!!");
    registry.add("mailer.bootstrap.email", () -> "admin@example.com");
    registry.add("mailer.bootstrap.password", () -> "changeme");
  }

  @LocalServerPort int port;

  @Autowired CampaignSendService sendService;
  @Autowired CampaignRepository campaigns;
  @Autowired CampaignListRepository campaignLists;
  @Autowired SendingPathRepository paths;
  @Autowired ListRepository lists;
  @Autowired MemberRepository members;
  @Autowired ListMemberRepository listMembers;
  @Autowired PersistingOpsHooks ops;
  @Autowired SecretStore secrets;

  private String pathId;

  @BeforeEach
  void seedPath() {
    pathId = UUID.randomUUID().toString();
    paths.save(
        new SendingPathEntity(
            pathId,
            "gm-" + pathId.substring(0, 8),
            "127.0.0.1",
            greenMail.getSmtp().getPort(),
            "mailer",
            secrets.create("mailer"),
            "from@localhost",
            "Mailer",
            false,
            Instant.now()));
  }

  @Test
  @DisplayName("R1/R4 GreenMail unsub removes member from all campaign lists")
  void greenMailUnsubMultiList() throws Exception {
    String listA = newList("A");
    String listB = newList("B");
    String listOut = newList("Out");
    String memberId = newMember("unsub@example.com");
    link(listA, memberId);
    link(listB, memberId);
    link(listOut, memberId);

    String cid = UUID.randomUUID().toString();
    campaigns.save(
        new CampaignEntity(cid, "Unsub", "<p>hi</p>", "hi", CampaignStatus.DRAFT, Instant.now()));
    campaignLists.save(new CampaignListEntity(cid, listA, 0));
    campaignLists.save(new CampaignListEntity(cid, listB, 1));

    sendService.queueAndSend(cid);
    sendService.processPendingBatch(10);
    assertTrue(greenMail.waitForIncomingEmail(15_000, 1));

    MimeMessage msg = greenMail.getReceivedMessages()[0];
    assertTrue(msg.getHeader("List-Unsubscribe")[0].contains("/unsubscribe/"));
    String html = String.valueOf(msg.getContent());
    if (msg.getContent() instanceof jakarta.mail.Multipart mp) {
      html = extractHtml(mp);
    }
    Matcher m = UNSUB.matcher(html);
    assertTrue(m.find(), "unsub link in body");
    String token = m.group(1);

    String page =
        RestClient.create("http://127.0.0.1:" + port)
            .get()
            .uri("/unsubscribe/{t}", token)
            .retrieve()
            .body(String.class);
    assertTrue(page.contains("Unsubscribed"));

    assertTrue(listMembers.findByListIdAndMemberId(listA, memberId).orElseThrow().isUnsubscribed());
    assertTrue(listMembers.findByListIdAndMemberId(listB, memberId).orElseThrow().isUnsubscribed());
    assertFalse(
        listMembers.findByListIdAndMemberId(listOut, memberId).orElseThrow().isUnsubscribed());
    assertTrue(members.findById(memberId).orElseThrow().isEnabled());
    assertTrue(ops.auditSnapshot().stream().anyMatch(s -> s.startsWith("unsubscribe:")));
    assertTrue(ops.auditSnapshot().stream().noneMatch(s -> s.contains(token)));

    String bad =
        RestClient.create("http://127.0.0.1:" + port)
            .get()
            .uri("/unsubscribe/{t}", "not-a-real-token")
            .retrieve()
            .body(String.class);
    assertTrue(bad.contains("Unable to unsubscribe"));
  }

  private static String extractHtml(jakarta.mail.Multipart mp) throws Exception {
    for (int i = 0; i < mp.getCount(); i++) {
      var part = mp.getBodyPart(i);
      if (part.isMimeType("text/html")) {
        return (String) part.getContent();
      }
      if (part.getContent() instanceof jakarta.mail.Multipart nested) {
        String inner = extractHtml(nested);
        if (inner != null) {
          return inner;
        }
      }
    }
    return "";
  }

  private String newList(String name) {
    String id = UUID.randomUUID().toString();
    lists.save(
        new ListEntity(id, name + "-" + id.substring(0, 8), pathId, null, null, null, null, Instant.now()));
    return id;
  }

  private String newMember(String email) {
    String id = UUID.randomUUID().toString();
    members.save(new MemberEntity(id, email, true, Instant.now()));
    return id;
  }

  private void link(String listId, String memberId) {
    listMembers.save(new ListMemberEntity(listId, memberId, false, Instant.now()));
  }
}
