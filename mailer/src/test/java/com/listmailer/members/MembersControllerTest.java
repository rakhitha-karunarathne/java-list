package com.listmailer.members;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.CampaignOpenEntity;
import com.listmailer.campaigns.CampaignOpenRepository;
import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.consent.ConsentService;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.ops.AuditLogEntity;
import com.listmailer.ops.AuditLogRepository;
import com.listmailer.users.ListAccessService;
import com.listmailer.users.UserListGrantEntity;
import com.listmailer.users.UserListGrantRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MembersControllerTest {

  @Mock MemberRepository members;
  @Mock ListMemberRepository listMembers;
  @Mock ListRepository lists;
  @Mock ListAccessService access;
  @Mock UserListGrantRepository grants;
  @Mock ConsentService consent;
  @Mock AuditLogRepository audit;
  @Mock OpsHooksPort ops;
  @Mock SendQueueRepository sendQueue;
  @Mock CampaignOpenRepository opens;
  @InjectMocks MembersController controller;

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void indexAdminReturnsAll() {
    when(access.isAdmin()).thenReturn(true);
    when(members.findAll())
        .thenReturn(
            List.of(
                new MemberEntity("m1", "a@example.com", true, Instant.now()),
                new MemberEntity("m2", "b@example.com", false, Instant.now())));

    List<MembersController.MemberSummary> rows = controller.index(null);

    assertEquals(2, rows.size());
    assertEquals("a@example.com", rows.get(0).email());
  }

  @Test
  void indexSearchUsesEmailContains() {
    when(access.isAdmin()).thenReturn(true);
    when(members.findByEmailContainingIgnoreCase("rakh"))
        .thenReturn(List.of(new MemberEntity("m1", "rakhitha@example.com", true, Instant.now())));

    List<MembersController.MemberSummary> rows = controller.index(" rakh ");

    assertEquals(1, rows.size());
    assertEquals("rakhitha@example.com", rows.get(0).email());
  }

  @Test
  void indexListManagerOnlyScopedMembers() {
    asUser("u1");
    when(access.isAdmin()).thenReturn(false);
    when(grants.findByUserId("u1"))
        .thenReturn(List.of(new UserListGrantEntity("u1", "L1", true, false)));
    when(listMembers.findByListIdIn(List.of("L1")))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", false, Instant.now())));
    when(members.findAll())
        .thenReturn(
            List.of(
                new MemberEntity("m1", "in@example.com", true, Instant.now()),
                new MemberEntity("m2", "out@example.com", true, Instant.now())));

    List<MembersController.MemberSummary> rows = controller.index(null);

    assertEquals(1, rows.size());
    assertEquals("m1", rows.get(0).id());
  }

  @Test
  void detailIncludesMembershipsSendOpenAndAudit() {
    when(members.findById("m1"))
        .thenReturn(
            Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(access.isAdmin()).thenReturn(true);
    when(listMembers.findByMemberId("m1"))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", false, Instant.now())));
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "News", "sp", null, null, null, null, Instant.now())));

    SendQueueEntity sent = new SendQueueEntity("q1", "c1", "m1", "a@example.com", "sp", "SENT");
    sent.setListId("L1");
    sent.setSubject("Hello");
    sent.setSentAt(Instant.parse("2026-02-01T10:00:00Z"));
    when(sendQueue.findByMemberId("m1")).thenReturn(List.of(sent));
    when(opens.findByMemberId("m1"))
        .thenReturn(
            List.of(
                new CampaignOpenEntity(
                    "o1", "c1", "m1", "q1", Instant.parse("2026-02-01T11:00:00Z"))));
    when(audit.findByMemberIdInOrderByCreatedAtDesc(List.of("m1")))
        .thenReturn(
            List.of(
                new AuditLogEntity(
                    "a-dup",
                    "campaign.sent",
                    "ignored duplicate",
                    "system",
                    "L1",
                    "m1",
                    Instant.parse("2026-02-01T10:00:00Z")),
                new AuditLogEntity(
                    "a1",
                    "member.enable",
                    "ok",
                    "admin",
                    null,
                    "m1",
                    Instant.parse("2026-01-15T09:00:00Z"))));

    MembersController.MemberDetail detail = controller.get("m1");

    assertEquals(1, detail.memberships().size());
    assertEquals("News", detail.memberships().get(0).listName());
    assertEquals(3, detail.timeline().size());
    assertEquals("campaign.open", detail.timeline().get(0).action());
    assertEquals("campaign.sent", detail.timeline().get(1).action());
    assertTrue(detail.timeline().get(1).detail().contains("subject=Hello"));
    assertEquals("member.enable", detail.timeline().get(2).action());
  }

  @Test
  void listManagerDetailHidesOtherListHistory() {
    asUser("u1");
    when(members.findById("m1"))
        .thenReturn(
            Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(access.isAdmin()).thenReturn(false);
    when(grants.findByUserId("u1"))
        .thenReturn(List.of(new UserListGrantEntity("u1", "L1", true, true)));
    when(listMembers.findByListIdIn(List.of("L1")))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", false, Instant.now())));
    when(listMembers.findByMemberId("m1"))
        .thenReturn(
            List.of(
                new ListMemberEntity("L1", "m1", false, Instant.now()),
                new ListMemberEntity("L2", "m1", false, Instant.now())));
    when(access.canAccess("L1")).thenReturn(true);
    when(access.canAccess("L2")).thenReturn(false);
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "News", "sp", null, null, null, null, Instant.now())));

    SendQueueEntity q1 = new SendQueueEntity("q1", "c1", "m1", "a@example.com", "sp", "SENT");
    q1.setListId("L1");
    q1.setSentAt(Instant.parse("2026-02-01T10:00:00Z"));
    SendQueueEntity q2 = new SendQueueEntity("q2", "c2", "m1", "a@example.com", "sp", "SENT");
    q2.setListId("L2");
    q2.setSentAt(Instant.parse("2026-02-01T12:00:00Z"));
    when(sendQueue.findByMemberId("m1")).thenReturn(List.of(q1, q2));
    when(opens.findByMemberId("m1"))
        .thenReturn(
            List.of(
                new CampaignOpenEntity("o1", "c1", "m1", "q1", Instant.parse("2026-02-01T11:00:00Z")),
                new CampaignOpenEntity(
                    "o2", "c2", "m1", "q2", Instant.parse("2026-02-01T13:00:00Z"))));
    when(audit.findByMemberIdInOrderByCreatedAtDesc(List.of("m1"))).thenReturn(List.of());

    MembersController.MemberDetail detail = controller.get("m1");

    assertEquals(1, detail.memberships().size());
    assertEquals(2, detail.timeline().size());
    assertEquals("campaign.open", detail.timeline().get(0).action());
    assertEquals("L1", detail.timeline().get(0).listId());
    assertEquals("campaign.sent", detail.timeline().get(1).action());
    assertEquals("L1", detail.timeline().get(1).listId());
  }

  @Test
  void patchDisableRequiresReasonAndAudits() {
    asUser("admin");
    MemberEntity member = new MemberEntity("m1", "a@example.com", true, Instant.now());
    when(members.findById("m1")).thenReturn(Optional.of(member));
    when(access.isAdmin()).thenReturn(true);
    when(listMembers.findByMemberId("m1")).thenReturn(List.of());
    when(sendQueue.findByMemberId("m1")).thenReturn(List.of());
    when(opens.findByMemberId("m1")).thenReturn(List.of());
    when(audit.findByMemberIdInOrderByCreatedAtDesc(List.of("m1"))).thenReturn(List.of());
    when(members.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ResponseStatusException missing =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.patch("m1", new MembersController.PatchBody(false, "  ", null)));
    assertEquals(HttpStatus.BAD_REQUEST, missing.getStatusCode());

    MembersController.MemberDetail detail =
        controller.patch("m1", new MembersController.PatchBody(false, "bounce", null));

    assertEquals(false, detail.enabled());
    assertEquals("bounce", detail.disableReason());
    verify(ops).audit(eq("member.disable"), eq("reason=bounce"), eq("admin"), isNull(), eq("m1"));
  }

  @Test
  void patchEnableRequiresConfirm() {
    asUser("admin");
    MemberEntity member = new MemberEntity("m1", "a@example.com", false, Instant.now());
    member.setDisableReason("old");
    when(members.findById("m1")).thenReturn(Optional.of(member));
    when(access.isAdmin()).thenReturn(true);

    ResponseStatusException missing =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.patch("m1", new MembersController.PatchBody(true, null, null)));
    assertEquals(HttpStatus.BAD_REQUEST, missing.getStatusCode());

    when(listMembers.findByMemberId("m1")).thenReturn(List.of());
    when(sendQueue.findByMemberId("m1")).thenReturn(List.of());
    when(opens.findByMemberId("m1")).thenReturn(List.of());
    when(audit.findByMemberIdInOrderByCreatedAtDesc(List.of("m1"))).thenReturn(List.of());
    when(members.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MembersController.MemberDetail detail =
        controller.patch("m1", new MembersController.PatchBody(true, null, true));

    assertEquals(true, detail.enabled());
    assertEquals("", detail.disableReason());
    verify(ops).audit(eq("member.enable"), eq("member=m1"), eq("admin"), isNull(), eq("m1"));
  }

  @Test
  void consentClearDelegates() {
    when(members.findById("m1"))
        .thenReturn(
            Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(access.isAdmin()).thenReturn(true);

    Map<String, String> res =
        controller.requestConsentClear("m1", new MembersController.ConsentClearBody(List.of("L1")));

    assertEquals("consent_sent", res.get("status"));
    verify(consent).requestClearUnsub("m1", List.of("L1"));
  }

  @Test
  void reAddRequiresConfirmAndManage() {
    when(members.findById("m1"))
        .thenReturn(
            Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(access.isAdmin()).thenReturn(true);
    when(access.canManage("L1")).thenReturn(false);
    ResponseStatusException forbidden =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.reAdd("m1", "L1", new MembersController.ReAddBody(true)));
    assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());

    when(access.canManage("L1")).thenReturn(true);
    ResponseStatusException confirm =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.reAdd("m1", "L1", new MembersController.ReAddBody(false)));
    assertEquals(HttpStatus.BAD_REQUEST, confirm.getStatusCode());

    when(listMembers.findByListIdAndMemberId("L1", "m1")).thenReturn(Optional.empty());
    when(listMembers.findByMemberId("m1")).thenReturn(List.of());
    when(sendQueue.findByMemberId("m1")).thenReturn(List.of());
    when(opens.findByMemberId("m1")).thenReturn(List.of());
    when(audit.findByMemberIdInOrderByCreatedAtDesc(List.of("m1"))).thenReturn(List.of());
    when(listMembers.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MembersController.MemberDetail detail =
        controller.reAdd("m1", "L1", new MembersController.ReAddBody(true));

    assertEquals("m1", detail.id());
    verify(listMembers).save(any(ListMemberEntity.class));
  }

  @Test
  void getUnknownMemberNotFound() {
    when(members.findById("missing")).thenReturn(Optional.empty());
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.get("missing"));
    assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
  }

  private static void asUser(String userId) {
    Jwt jwt =
        Jwt.withTokenValue("t").header("alg", "none").subject(userId).claim("roles", List.of()).build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }
}
