package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.listmailer.catalogs.SendingPathEntity;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MariaDBContainer;

/**
 * GreenMail + Testcontainers MariaDB regression for Campaign compose → send → pixel and
 * Stop/Requeue / Finished-Add-List (story 5.10).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class CampaignsE2ETest {

  private static final Pattern PIXEL = Pattern.compile("/t/([0-9a-fA-F-]{36})");

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
    registry.add("mailer.bootstrap.email", () -> "admin@example.com");
    registry.add("mailer.bootstrap.password", () -> "changeme");
  }

  @LocalServerPort int port;

  @Autowired CampaignSendService sendService;
  @Autowired CampaignRepository campaigns;
  @Autowired CampaignListRepository campaignLists;
  @Autowired CampaignOpenRepository opens;
  @Autowired SendQueueRepository queue;
  @Autowired SendingPathRepository paths;
  @Autowired ListRepository lists;
  @Autowired MemberRepository members;
  @Autowired ListMemberRepository listMembers;

  private String pathId;
  private RestClient api;

  @BeforeEach
  void seedPath() {
    pathId = UUID.randomUUID().toString();
    int smtpPort = greenMail.getSmtp().getPort();
    paths.save(
        new SendingPathEntity(
            pathId,
            "gm-" + pathId.substring(0, 8),
            "127.0.0.1",
            smtpPort,
            "mailer",
            "mailer",
            "from@localhost",
            "Mailer",
            false,
            Instant.now()));
    @SuppressWarnings("unchecked")
    Map<String, Object> login =
        RestClient.create("http://127.0.0.1:" + port)
            .post()
            .uri("/api/auth/login")
            .body(Map.of("email", "admin@example.com", "password", "changeme"))
            .retrieve()
            .body(Map.class);
    String token = (String) login.get("token");
    assertTrue(token != null && !token.isBlank(), "admin login token");
    api =
        RestClient.builder()
            .baseUrl("http://127.0.0.1:" + port)
            .defaultHeader("Authorization", "Bearer " + token)
            .build();
  }

  @Test
  @DisplayName("R1/R8 compose→queue→send→Finished with multi-list dedupe")
  void composeQueueSendFinishedDedupe() throws Exception {
    String listA = newList("A");
    String listB = newList("B");
    String shared = newMember("shared@example.com");
    String onlyA = newMember("only-a@example.com");
    String onlyB = newMember("only-b@example.com");
    link(listA, shared);
    link(listA, onlyA);
    link(listB, shared);
    link(listB, onlyB);

    CampaignEntity c = newCampaign("Dedup", listA, listB);
    sendService.queueAndSend(c.getId());
    assertEquals(CampaignStatus.QUEUED, campaigns.findById(c.getId()).orElseThrow().getStatus());
    assertEquals(3, queue.findByCampaignId(c.getId()).size());

    drain(c.getId());
    assertEquals(CampaignStatus.FINISHED, campaigns.findById(c.getId()).orElseThrow().getStatus());
    assertEquals(3, sentCount(c.getId()));
    assertTrue(greenMail.waitForIncomingEmail(15_000, 3));
    Set<String> tos = recipientEmails();
    assertEquals(Set.of("shared@example.com", "only-a@example.com", "only-b@example.com"), tos);
  }

  @Test
  @DisplayName("R7 open pixel recorded without blocking SMTP")
  void openPixelRecorded() throws Exception {
    String listId = newList("Pixel");
    String memberId = newMember("pixel@example.com");
    link(listId, memberId);
    CampaignEntity c = newCampaign("Pixel", listId);
    api.post().uri("/api/campaigns/{id}/queue", c.getId()).retrieve().toBodilessEntity();
    drain(c.getId());
    assertTrue(greenMail.waitForIncomingEmail(15_000, 1));

    String html = htmlBody(greenMail.getReceivedMessages()[0]);
    Matcher m = PIXEL.matcher(html);
    assertTrue(m.find(), "pixel URL in HTML");
    String token = m.group(1);

    ResponseEntity<byte[]> gif =
        RestClient.create("http://127.0.0.1:" + port)
            .get()
            .uri("/t/{t}", token)
            .retrieve()
            .toEntity(byte[].class);
    assertEquals(200, gif.getStatusCode().value());
    assertTrue(gif.getBody() != null && gif.getBody().length > 0);

    awaitOpens(c.getId(), 1);
    @SuppressWarnings("unchecked")
    Map<String, Object> detail =
        api.get().uri("/api/campaigns/{id}", c.getId()).retrieve().body(Map.class);
    assertTrue(((Number) detail.get("uniqueOpens")).longValue() >= 1);
    assertTrue(((Number) detail.get("totalOpens")).longValue() >= 1);
  }

  @Test
  @DisplayName("R8 Stop mid-send → Stopped → Requeue → Finished without duplicate sends")
  void stopThenRequeue() throws Exception {
    String listId = newList("Stop");
    for (int i = 0; i < 4; i++) {
      link(listId, newMember("stop" + i + "@example.com"));
    }
    CampaignEntity c = newCampaign("Stop", listId);
    api.post().uri("/api/campaigns/{id}/queue", c.getId()).retrieve().toBodilessEntity();
    sendService.processPendingBatch(1);
    assertEquals(CampaignStatus.SENDING, campaigns.findById(c.getId()).orElseThrow().getStatus());
    assertEquals(1, sentCount(c.getId()));
    assertEquals(3, pendingCount(c.getId()));

    @SuppressWarnings("unchecked")
    Map<String, Object> stopped =
        api.post().uri("/api/campaigns/{id}/stop", c.getId()).retrieve().body(Map.class);
    assertEquals("STOPPED", stopped.get("status"));
    assertEquals(0, sendService.processPendingBatch(10));
    assertEquals(3, pendingCount(c.getId()));

    api.post().uri("/api/campaigns/{id}/requeue", c.getId()).retrieve().toBodilessEntity();
    drain(c.getId());
    assertEquals(CampaignStatus.FINISHED, campaigns.findById(c.getId()).orElseThrow().getStatus());
    assertEquals(4, sentCount(c.getId()));
    assertTrue(greenMail.waitForIncomingEmail(15_000, 4));
    assertEquals(4, greenMail.getReceivedMessages().length);
  }

  @Test
  @DisplayName("R8 Finished Requeue after Add List sends only the new address")
  void finishedAddListRequeue() throws Exception {
    String list1 = newList("L1");
    String already = newMember("already@example.com");
    link(list1, already);
    CampaignEntity c = newCampaign("AddList", list1);
    api.post().uri("/api/campaigns/{id}/queue", c.getId()).retrieve().toBodilessEntity();
    drain(c.getId());
    assertEquals(CampaignStatus.FINISHED, campaigns.findById(c.getId()).orElseThrow().getStatus());
    assertTrue(greenMail.waitForIncomingEmail(15_000, 1));
    greenMail.purgeEmailFromAllMailboxes();

    String list2 = newList("L2");
    String neu = newMember("new@example.com");
    link(list2, already);
    link(list2, neu);
    api.post()
        .uri("/api/campaigns/{id}/lists", c.getId())
        .body(Map.of("listId", list2))
        .retrieve()
        .toBodilessEntity();

    api.post().uri("/api/campaigns/{id}/requeue", c.getId()).retrieve().toBodilessEntity();
    assertEquals(1, pendingCount(c.getId()));
    drain(c.getId());
    @SuppressWarnings("unchecked")
    Map<String, Object> detail =
        api.get().uri("/api/campaigns/{id}", c.getId()).retrieve().body(Map.class);
    assertEquals("FINISHED", detail.get("status"));
    assertEquals(2, ((Number) detail.get("sentCount")).longValue());
    assertTrue(greenMail.waitForIncomingEmail(15_000, 1));
    assertEquals(Set.of("new@example.com"), recipientEmails());
  }

  private void drain(String campaignId) {
    for (int i = 0; i < 50 && pendingCount(campaignId) > 0; i++) {
      sendService.processPendingBatch(50);
    }
    if (pendingCount(campaignId) > 0) {
      fail("PENDING rows remain for " + campaignId);
    }
  }

  private long sentCount(String campaignId) {
    return queue.findByCampaignId(campaignId).stream()
        .filter(r -> "SENT".equals(r.getStatus()))
        .count();
  }

  private long pendingCount(String campaignId) {
    return queue.findByCampaignId(campaignId).stream()
        .filter(r -> "PENDING".equals(r.getStatus()))
        .count();
  }

  private void awaitOpens(String campaignId, long min) throws InterruptedException {
    for (int i = 0; i < 40; i++) {
      if (opens.countByCampaignId(campaignId) >= min) {
        return;
      }
      Thread.sleep(50);
    }
    fail("opens not recorded for " + campaignId);
  }

  private Set<String> recipientEmails() throws Exception {
    return Arrays.stream(greenMail.getReceivedMessages())
        .map(
            msg -> {
              try {
                return msg.getAllRecipients()[0].toString().toLowerCase(Locale.ROOT);
              } catch (Exception e) {
                throw new IllegalStateException(e);
              }
            })
        .collect(Collectors.toSet());
  }

  private static String htmlBody(MimeMessage msg) throws Exception {
    Object content = msg.getContent();
    if (content instanceof String s) {
      return s;
    }
    if (content instanceof jakarta.mail.Multipart mp) {
      for (int i = 0; i < mp.getCount(); i++) {
        var part = mp.getBodyPart(i);
        if (part.isMimeType("text/html")) {
          return (String) part.getContent();
        }
        if (part.getContent() instanceof jakarta.mail.Multipart nested) {
          for (int j = 0; j < nested.getCount(); j++) {
            var inner = nested.getBodyPart(j);
            if (inner.isMimeType("text/html")) {
              return (String) inner.getContent();
            }
          }
        }
      }
    }
    return String.valueOf(content);
  }

  private String newList(String name) {
    String id = UUID.randomUUID().toString();
    lists.save(new ListEntity(id, name + "-" + id.substring(0, 8), pathId, null, null, null, null, Instant.now()));
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

  private CampaignEntity newCampaign(String subject, String... listIds) {
    String id = UUID.randomUUID().toString();
    CampaignEntity c =
        campaigns.save(
            new CampaignEntity(
                id,
                subject,
                "<p>hello</p>",
                "hello",
                CampaignStatus.DRAFT,
                Instant.now()));
    int i = 0;
    for (String listId : listIds) {
      campaignLists.save(new CampaignListEntity(id, listId, i++));
    }
    return c;
  }
}
