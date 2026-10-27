package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import com.listmailer.users.ListAccessService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BounceEventsControllerTest {

  @Mock BounceEventRepository events;
  @Mock SendQueueRepository queue;
  @Mock CampaignRepository campaigns;
  @Mock MemberRepository members;
  @Mock ListMemberRepository listMembers;
  @Mock ListAccessService access;
  @InjectMocks BounceEventsController controller;

  @Test
  void indexReadsBounceEventsNewestFirstAndBackfillsError() {
    when(access.isListSenderOnly()).thenReturn(false);
    when(access.isAdmin()).thenReturn(true);
    BounceEventEntity older =
        new BounceEventEntity(
            "b1",
            "mb",
            Instant.parse("2026-01-01T00:00:00Z"),
            null,
            "Undelivered",
            "mailer-daemon@x",
            "Diagnostic-Code: smtp; 552 mailbox full",
            "raw".getBytes(StandardCharsets.UTF_8),
            "UNCORRELATED");
    BounceEventEntity newer =
        new BounceEventEntity(
            "b2",
            "mb",
            Instant.parse("2026-02-01T00:00:00Z"),
            null,
            "Hi",
            "mailer-daemon@x",
            "",
            "Status: 5.1.1\nuser unknown\n".getBytes(StandardCharsets.UTF_8),
            "CORRELATED");
    newer.setMemberId("m1");
    newer.setSendQueueId("q1");
    newer.setClassification("HARD");
    when(events.findAllByOrderByReceivedAtDesc()).thenReturn(List.of(newer, older));
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "blake@example.com", true, Instant.now())));
    when(queue.findById("q1"))
        .thenReturn(
            Optional.of(new SendQueueEntity("q1", "c1", "m1", "blake@example.com", "p", "SENT")));
    when(campaigns.findById("c1"))
        .thenReturn(
            Optional.of(
                new CampaignEntity(
                    "c1", "October tips", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now())));
    when(events.save(any())).thenAnswer(inv -> inv.getArgument(0));

    List<BounceEventsController.BounceMessageView> rows = controller.index(null);

    assertEquals(2, rows.size());
    assertEquals("b2", rows.get(0).id());
    assertEquals("blake@example.com", rows.get(0).email());
    assertEquals("c1", rows.get(0).campaignId());
    assertEquals("October tips", rows.get(0).campaignSubject());
    assertTrue(rows.get(0).correlated());
    assertEquals("HARD", rows.get(0).classification());
    assertTrue(rows.get(0).error().toLowerCase().contains("5.1.1") || rows.get(0).error().contains("user"));
    assertEquals("b1", rows.get(1).id());
    assertFalse(rows.get(1).correlated());
    assertEquals("552 mailbox full", rows.get(1).error());
    verify(events).save(older);
  }

  @Test
  void listManagerOnlySeesManagedMembers() {
    when(access.isListSenderOnly()).thenReturn(false);
    when(access.isAdmin()).thenReturn(false);
    when(access.isListManager()).thenReturn(true);
    when(access.managedListIds()).thenReturn(List.of("L1"));
    when(listMembers.findByListIdIn(List.of("L1")))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", false, Instant.now())));
    BounceEventEntity mine =
        event("mine", "CORRELATED", "m1", Instant.parse("2026-03-01T00:00:00Z"));
    BounceEventEntity other =
        event("other", "CORRELATED", "m2", Instant.parse("2026-03-02T00:00:00Z"));
    when(events.findAllByOrderByReceivedAtDesc()).thenReturn(List.of(other, mine));

    List<BounceEventsController.BounceMessageView> rows = controller.index("all");

    assertEquals(1, rows.size());
    assertEquals("mine", rows.get(0).id());
  }

  @Test
  void senderForbidden() {
    when(access.isListSenderOnly()).thenReturn(true);
    assertThrows(ResponseStatusException.class, () -> controller.index(null));
    verify(events, never()).findAllByOrderByReceivedAtDesc();
  }

  @Test
  void filterUncorrelated() {
    when(access.isListSenderOnly()).thenReturn(false);
    when(access.isAdmin()).thenReturn(true);
    BounceEventEntity u =
        event("u1", "UNCORRELATED", null, Instant.parse("2026-04-01T00:00:00Z"));
    BounceEventEntity c =
        event("c1", "CORRELATED", "m1", Instant.parse("2026-04-02T00:00:00Z"));
    when(events.findAllByOrderByReceivedAtDesc()).thenReturn(List.of(c, u));

    List<BounceEventsController.BounceMessageView> rows = controller.index("uncorrelated");

    assertEquals(1, rows.size());
    assertEquals("u1", rows.get(0).id());
  }

  private static BounceEventEntity event(
      String id, String status, String memberId, Instant receivedAt) {
    BounceEventEntity e =
        new BounceEventEntity(
            id,
            "mb",
            receivedAt,
            null,
            "subj",
            "from@x",
            "Diagnostic-Code: smtp; 550 fail",
            new byte[0],
            status);
    e.setMemberId(memberId);
    e.setClassification("SOFT");
    e.setErrorText("550 fail");
    if ("CORRELATED".equals(status)) {
      e.setSendQueueId("q-" + id);
    }
    return e;
  }
}
