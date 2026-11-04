package com.listmailer.members;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class MembersControllerDetailTest {

  @Mock MemberRepository members;
  @Mock ListMemberRepository listMembers;
  @Mock ListRepository lists;
  @Mock ListAccessService access;
  @Mock UserListGrantRepository grants;
  @Mock ConsentService consent;
  @Mock AuditLogRepository audit;
  @Mock OpsHooksPort ops;
  @InjectMocks MembersController controller;

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void detailIncludesMembershipsAndTimeline() {
    MemberEntity member =
        new MemberEntity("m1", "a@example.com", true, Instant.parse("2026-01-01T00:00:00Z"));
    when(members.findById("m1")).thenReturn(Optional.of(member));
    when(access.isAdmin()).thenReturn(true);
    when(listMembers.findByMemberId("m1"))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", false, Instant.now())));
    when(lists.findById("L1"))
        .thenReturn(Optional.of(new ListEntity("L1", "News", "sp", null, null, null, null, Instant.now())));
    when(audit.findByMemberIdInOrderByCreatedAtDesc(List.of("m1")))
        .thenReturn(
            List.of(
                new AuditLogEntity(
                    "a1",
                    "campaign.sent",
                    "subject",
                    "system",
                    "L1",
                    "m1",
                    Instant.parse("2026-02-01T12:00:00Z"))));

    MembersController.MemberDetail detail = controller.get("m1");

    assertEquals(1, detail.memberships().size());
    assertEquals("News", detail.memberships().get(0).listName());
    assertEquals(false, detail.memberships().get(0).unsubscribed());
    assertEquals(1, detail.timeline().size());
    assertEquals("campaign.sent", detail.timeline().get(0).action());
  }

  @Test
  void listManagerTimelineHidesOtherLists() {
    Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject("u1").claim("roles", List.of()).build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    MemberEntity member =
        new MemberEntity("m1", "a@example.com", true, Instant.parse("2026-01-01T00:00:00Z"));
    when(members.findById("m1")).thenReturn(Optional.of(member));
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
        .thenReturn(Optional.of(new ListEntity("L1", "News", "sp", null, null, null, null, Instant.now())));
    when(audit.findByMemberIdInOrderByCreatedAtDesc(List.of("m1")))
        .thenReturn(
            List.of(
                new AuditLogEntity(
                    "a1", "campaign.sent", "L1", "system", "L1", "m1", Instant.now()),
                new AuditLogEntity(
                    "a2", "campaign.sent", "L2", "system", "L2", "m1", Instant.now()),
                new AuditLogEntity(
                    "a3", "member.enable", "ok", "admin", null, "m1", Instant.now())));

    MembersController.MemberDetail detail = controller.get("m1");

    assertEquals(1, detail.memberships().size());
    assertEquals("L1", detail.memberships().get(0).listId());
    assertEquals(2, detail.timeline().size());
    assertEquals("campaign.sent", detail.timeline().get(0).action());
    assertEquals("L1", detail.timeline().get(0).listId());
    assertEquals("member.enable", detail.timeline().get(1).action());
  }
}
