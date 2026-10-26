package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CampaignSendServiceTest {

  @Mock CampaignRepository campaigns;
  @Mock CampaignListRepository campaignLists;
  @Mock CampaignAttachmentRepository attachments;
  @Mock SendQueueRepository queue;
  @Mock ListRepository lists;
  @Mock ListMemberRepository listMembers;
  @Mock MemberRepository members;
  @Mock SmtpPort smtp;
  @InjectMocks CampaignSendService service;

  @Test
  void dedupesSameAddressAcrossListsToOneQueueRow() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(
            List.of(
                new CampaignListEntity("c1", "list-a", 0),
                new CampaignListEntity("c1", "list-b", 1)));
    when(lists.findById("list-a"))
        .thenReturn(
            Optional.of(
                new ListEntity("list-a", "A", "path-1", null, null, null, null, Instant.now())));
    when(listMembers.findByListId("list-a"))
        .thenReturn(List.of(new ListMemberEntity("list-a", "m1", false, Instant.now())));
    when(listMembers.findByListId("list-b"))
        .thenReturn(List.of(new ListMemberEntity("list-b", "m2", false, Instant.now())));
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "Same@Example.com", true, Instant.now())));
    when(members.findById("m2"))
        .thenReturn(Optional.of(new MemberEntity("m2", "same@example.com", true, Instant.now())));
    when(attachments.findByCampaignId("c1")).thenReturn(List.of());
    List<SendQueueEntity> enqueued = new ArrayList<>();
    when(queue.save(any()))
        .thenAnswer(
            inv -> {
              SendQueueEntity row = inv.getArgument(0);
              if ("PENDING".equals(row.getStatus())
                  && enqueued.stream().noneMatch(e -> e.getId().equals(row.getId()))) {
                enqueued.add(row);
              }
              return row;
            });
    when(queue.findByCampaignId("c1")).thenAnswer(inv -> List.copyOf(enqueued));

    service.queueAndSend("c1");

    assertEquals(1, enqueued.size());
    ArgumentCaptor<SmtpPort.SmtpMessage> cap = ArgumentCaptor.forClass(SmtpPort.SmtpMessage.class);
    verify(smtp, times(1)).send(cap.capture());
    assertEquals("Same@Example.com", cap.getValue().toEmail());
    assertEquals("path-1", cap.getValue().sendingPathId());
    assertEquals("<p>x</p>", cap.getValue().htmlBody());
  }

  @Test
  void usesFirstOrderedListSendingPath() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(
            List.of(
                new CampaignListEntity("c1", "list-b", 0),
                new CampaignListEntity("c1", "list-a", 1)));
    when(lists.findById("list-b"))
        .thenReturn(
            Optional.of(
                new ListEntity("list-b", "B", "path-b", null, null, null, null, Instant.now())));
    when(listMembers.findByListId("list-b"))
        .thenReturn(List.of(new ListMemberEntity("list-b", "m1", false, Instant.now())));
    when(listMembers.findByListId("list-a")).thenReturn(List.of());
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(attachments.findByCampaignId("c1")).thenReturn(List.of());
    List<SendQueueEntity> enqueued = new ArrayList<>();
    when(queue.save(any()))
        .thenAnswer(
            inv -> {
              enqueued.add(inv.getArgument(0));
              return inv.getArgument(0);
            });
    when(queue.findByCampaignId("c1")).thenAnswer(inv -> List.copyOf(enqueued));

    service.queueAndSend("c1");

    ArgumentCaptor<SmtpPort.SmtpMessage> cap = ArgumentCaptor.forClass(SmtpPort.SmtpMessage.class);
    verify(smtp).send(cap.capture());
    assertEquals("path-b", cap.getValue().sendingPathId());
  }

  @Test
  void rejectsEmptyBodiesOnQueue() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "  ", "", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));

    assertThrows(ResponseStatusException.class, () -> service.queueAndSend("c1"));
  }

  @Test
  void forwardsAttachmentsToSmtp() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(new CampaignListEntity("c1", "list-a", 0)));
    when(lists.findById("list-a"))
        .thenReturn(
            Optional.of(
                new ListEntity("list-a", "A", "path-1", null, null, null, null, Instant.now())));
    when(listMembers.findByListId("list-a"))
        .thenReturn(List.of(new ListMemberEntity("list-a", "m1", false, Instant.now())));
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    byte[] bytes = new byte[] {1, 2, 3};
    when(attachments.findByCampaignId("c1"))
        .thenReturn(
            List.of(
                new CampaignAttachmentEntity(
                    "att1", "c1", "note.txt", "text/plain", bytes)));
    List<SendQueueEntity> enqueued = new ArrayList<>();
    when(queue.save(any()))
        .thenAnswer(
            inv -> {
              enqueued.add(inv.getArgument(0));
              return inv.getArgument(0);
            });
    when(queue.findByCampaignId("c1")).thenAnswer(inv -> List.copyOf(enqueued));

    service.queueAndSend("c1");

    ArgumentCaptor<SmtpPort.SmtpMessage> cap = ArgumentCaptor.forClass(SmtpPort.SmtpMessage.class);
    verify(smtp).send(cap.capture());
    assertEquals(1, cap.getValue().attachments().size());
    assertEquals("note.txt", cap.getValue().attachments().get(0).filename());
    assertEquals(3, cap.getValue().attachments().get(0).content().length);
  }
}
