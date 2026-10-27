package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.token.OpaqueTokenHelper;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import java.time.Instant;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
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
  @Mock OpsHooksPort ops;
  @Mock ParametersPort parameters;
  @Mock CampaignHtmlRewriter htmlRewriter;
  @Mock RemoteImageFetchService imageFetch;
  @Mock OpaqueTokenHelper opaqueTokens;
  @Mock SendingPathRepository sendingPaths;
  @Mock com.listmailer.ops.ActivityLogService activity;
  @Mock TransactionTemplate transactions;
  @Mock SendPauseCache pauseCache;
  @InjectMocks CampaignSendService service;

  @BeforeEach
  void stubPublicBase() {
    lenient()
        .when(transactions.execute(any()))
        .thenAnswer(
            inv -> {
              TransactionCallback<?> callback = inv.getArgument(0);
              return callback.doInTransaction(
                  new org.springframework.transaction.support.SimpleTransactionStatus());
            });
    lenient()
        .when(parameters.get(ParameterKeys.PUBLIC_BASE_URL))
        .thenReturn(Optional.of("https://mail.example"));
    lenient()
        .when(parameters.get(ParameterKeys.IMAGE_FETCH_MODE))
        .thenReturn(Optional.of("Off"));
    lenient()
        .when(htmlRewriter.rewrite(any(), anyString(), anyString(), any()))
        .thenAnswer(inv -> inv.getArgument(0));
    lenient()
        .when(imageFetch.ingest(anyString(), any(), any(), any()))
        .thenAnswer(inv -> inv.getArgument(1));
    lenient().when(opaqueTokens.seal(any())).thenReturn("opaque-unsub-token");
    lenient().when(pauseCache.globalPaused()).thenReturn(false);
    lenient().when(pauseCache.campaignPaused(anyString())).thenReturn(false);
    lenient().when(pauseCache.listPaused(any())).thenReturn(false);
    lenient().when(queue.findByStatus("FAILED_RETRY_PENDING")).thenReturn(List.of());
    lenient().when(queue.findById(anyString())).thenReturn(Optional.empty());
    lenient().when(queue.save(any())).thenAnswer(inv -> inv.getArgument(0));
  }

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
    when(queue.findByStatus("PENDING"))
        .thenAnswer(
            inv ->
                enqueued.stream().filter(r -> "PENDING".equals(r.getStatus())).toList());

    service.queueAndSend("c1");
    assertEquals(1, enqueued.size());
    assertEquals(CampaignStatus.QUEUED, campaign.getStatus());

    service.processPendingBatch(10);

    ArgumentCaptor<SmtpPort.SmtpMessage> cap = ArgumentCaptor.forClass(SmtpPort.SmtpMessage.class);
    verify(smtp, times(1)).send(cap.capture());
    assertEquals("Same@Example.com", cap.getValue().toEmail());
    assertEquals("path-1", cap.getValue().sendingPathId());
    org.junit.jupiter.api.Assertions.assertTrue(cap.getValue().htmlBody().contains("/t/"));
    org.junit.jupiter.api.Assertions.assertTrue(
        cap.getValue().htmlBody().contains("/unsubscribe/"));
    assertEquals(CampaignStatus.FINISHED, campaign.getStatus());
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
    when(queue.findByStatus("PENDING"))
        .thenAnswer(
            inv ->
                enqueued.stream().filter(r -> "PENDING".equals(r.getStatus())).toList());

    service.queueAndSend("c1");
    service.processPendingBatch(10);

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
  void testSendUsesFirstPathPrefixesSubjectSkipsQueue() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(
            List.of(
                new CampaignListEntity("c1", "list-b", 0),
                new CampaignListEntity("c1", "list-a", 1)));
    when(lists.findById("list-b"))
        .thenReturn(
            Optional.of(
                new ListEntity("list-b", "B", "path-b", null, null, null, null, Instant.now())));
    when(attachments.findByCampaignId("c1")).thenReturn(List.of());

    int n = service.testSend("c1", List.of("anyone@example.com"));

    assertEquals(1, n);
    ArgumentCaptor<SmtpPort.SmtpMessage> cap = ArgumentCaptor.forClass(SmtpPort.SmtpMessage.class);
    verify(smtp, times(1)).send(cap.capture());
    assertEquals("path-b", cap.getValue().sendingPathId());
    assertEquals("[TEST] Hi", cap.getValue().subject());
    verify(queue, times(0)).save(any());
    assertEquals(CampaignStatus.DRAFT, campaign.getStatus());
  }

  @Test
  void testSendRejectsInvalidAddress() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(new CampaignListEntity("c1", "list-a", 0)));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> service.testSend("c1", List.of("not-an-email")));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void testSendFailureEmitsOpsHooks() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(new CampaignListEntity("c1", "list-a", 0)));
    when(lists.findById("list-a"))
        .thenReturn(
            Optional.of(
                new ListEntity("list-a", "A", "path-1", null, null, null, null, Instant.now())));
    when(attachments.findByCampaignId("c1")).thenReturn(List.of());
    org.mockito.Mockito.doThrow(new IllegalStateException("smtp down")).when(smtp).send(any());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.testSend("c1", List.of("a@b.com")));
    assertEquals(502, ex.getStatusCode().value());
    verify(ops).opsLog(org.mockito.ArgumentMatchers.eq("send.test"), org.mockito.ArgumentMatchers.contains("smtp down"));
    verify(ops).attentionAdmin(org.mockito.ArgumentMatchers.contains("Test send"), org.mockito.ArgumentMatchers.anyString());
  }

  @Test
  void finishedRequeueRequiresListNotYetIncluded() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    CampaignListEntity linked = new CampaignListEntity("c1", "list-a", 0);
    linked.setIncludedInPass(true);
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(linked));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.queueAndSend("c1"));
    assertEquals(409, ex.getStatusCode().value());
  }

  @Test
  void finishedRequeueSkipsAddressAlreadySentUnderOtherMember() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    CampaignListEntity oldList = new CampaignListEntity("c1", "list-a", 0);
    oldList.setIncludedInPass(true);
    CampaignListEntity newList = new CampaignListEntity("c1", "list-b", 1);
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(oldList, newList));
    when(lists.findById("list-a"))
        .thenReturn(
            Optional.of(
                new ListEntity("list-a", "A", "path-1", null, null, null, null, Instant.now())));
    when(lists.findById("list-b"))
        .thenReturn(
            Optional.of(
                new ListEntity("list-b", "B", "path-1", null, null, null, null, Instant.now())));
    when(listMembers.findByListId("list-a"))
        .thenReturn(List.of(new ListMemberEntity("list-a", "m1", false, Instant.now())));
    when(listMembers.findByListId("list-b"))
        .thenReturn(
            List.of(
                new ListMemberEntity("list-b", "m2", false, Instant.now()),
                new ListMemberEntity("list-b", "m3", false, Instant.now())));
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(members.findById("m2"))
        .thenReturn(Optional.of(new MemberEntity("m2", "a@example.com", true, Instant.now())));
    when(members.findById("m3"))
        .thenReturn(Optional.of(new MemberEntity("m3", "b@example.com", true, Instant.now())));
    SendQueueEntity prior =
        new SendQueueEntity("q1", "c1", "m1", "a@example.com", "path-1", "SENT");
    List<SendQueueEntity> enqueued = new ArrayList<>();
    enqueued.add(prior);
    when(queue.save(any()))
        .thenAnswer(
            inv -> {
              SendQueueEntity row = inv.getArgument(0);
              if (enqueued.stream().noneMatch(e -> e.getId().equals(row.getId()))) {
                enqueued.add(row);
              }
              return row;
            });
    when(queue.findByCampaignId("c1")).thenAnswer(inv -> List.copyOf(enqueued));
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.queueAndSend("c1");

    assertEquals(2, enqueued.size());
    assertEquals("m3", enqueued.get(1).getMemberId());
    assertEquals(CampaignStatus.QUEUED, campaign.getStatus());
    assertTrue(newList.isIncludedInPass());
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
    when(queue.findByStatus("PENDING"))
        .thenAnswer(
            inv ->
                enqueued.stream().filter(r -> "PENDING".equals(r.getStatus())).toList());

    service.queueAndSend("c1");
    service.processPendingBatch(10);

    ArgumentCaptor<SmtpPort.SmtpMessage> cap = ArgumentCaptor.forClass(SmtpPort.SmtpMessage.class);
    verify(smtp).send(cap.capture());
    assertEquals(1, cap.getValue().attachments().size());
    assertEquals("note.txt", cap.getValue().attachments().get(0).filename());
    assertEquals(3, cap.getValue().attachments().get(0).content().length);
  }

  @Test
  void stopsWhenDeadlineAlreadyPassed() {
    when(queue.findByStatus("PENDING"))
        .thenReturn(
            List.of(new SendQueueEntity("q1", "c1", "m1", "a@example.com", "path-1", "PENDING")));

    assertEquals(0, service.processPendingBatch(10, 0, Instant.now().minusSeconds(1)));

    verify(smtp, times(0)).send(any());
  }

  @Test
  void globalPauseSkipsBatch() {
    when(pauseCache.globalPaused()).thenReturn(true);

    assertEquals(0, service.processPendingBatch(10));

    verify(smtp, times(0)).send(any());
  }

  @Test
  void smtpFailureMarksRetryAndContinues() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.SENDING, Instant.now());
    SendQueueEntity fail =
        new SendQueueEntity("q1", "c1", "m1", "bad@example.com", "path-1", "PENDING");
    SendQueueEntity ok =
        new SendQueueEntity("q2", "c1", "m2", "ok@example.com", "path-1", "PENDING");
    when(queue.findByStatus("PENDING")).thenReturn(new ArrayList<>(List.of(fail, ok)));
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(attachments.findByCampaignId("c1")).thenReturn(List.of());
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1")).thenReturn(List.of());
    when(queue.findById(anyString()))
        .thenAnswer(
            inv -> {
              String id = inv.getArgument(0);
              if ("q1".equals(id)) {
                return Optional.of(fail);
              }
              if ("q2".equals(id)) {
                return Optional.of(ok);
              }
              return Optional.empty();
            });
    when(queue.findByCampaignId("c1")).thenAnswer(inv -> List.of(fail, ok));
    when(parameters.get(ParameterKeys.SEND_RETRY_COUNT)).thenReturn(Optional.of("3"));
    when(parameters.get(ParameterKeys.SEND_RETRY_DELAY_MINUTES)).thenReturn(Optional.of("5"));
    doAnswer(
            inv -> {
              SmtpPort.SmtpMessage msg = inv.getArgument(0);
              if (msg.toEmail().startsWith("bad")) {
                throw new RuntimeException("smtp down");
              }
              return null;
            })
        .when(smtp)
        .send(any());

    int sent = service.processPendingBatch(10, 0L, null);

    assertEquals("FAILED_RETRY_PENDING", fail.getStatus());
    assertEquals("SENT", ok.getStatus());
    assertEquals(1, sent);
  }

  @Test
  void exhaustedRetriesBecomeSendFailed() {
    SendQueueEntity row =
        new SendQueueEntity("q1", "c1", "m1", "bad@example.com", "path-1", "FAILED_RETRY_PENDING");
    row.setRetryAttempts(2);
    row.setNextAttemptAt(Instant.now().minusSeconds(1));
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.SENDING, Instant.now());
    when(queue.findByStatus("PENDING")).thenReturn(List.of());
    when(queue.findByStatus("FAILED_RETRY_PENDING")).thenReturn(List.of(row));
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(attachments.findByCampaignId("c1")).thenReturn(List.of());
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1")).thenReturn(List.of());
    when(queue.findById("q1")).thenReturn(Optional.of(row));
    when(parameters.get(ParameterKeys.SEND_RETRY_COUNT)).thenReturn(Optional.of("3"));
    when(parameters.get(ParameterKeys.SEND_RETRY_DELAY_MINUTES)).thenReturn(Optional.of("5"));
    doThrow(new RuntimeException("smtp down")).when(smtp).send(any());

    assertEquals(0, service.processPendingBatch(10));

    assertEquals("SEND_FAILED", row.getStatus());
    assertEquals(3, row.getRetryAttempts());
  }

  @Test
  void retryFailedRequeuesSendFailedOnly() {
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now());
    SendQueueEntity failed =
        new SendQueueEntity("q1", "c1", "m1", "a@example.com", "path-1", "SEND_FAILED");
    failed.setRetryAttempts(3);
    SendQueueEntity retrying =
        new SendQueueEntity("q2", "c1", "m2", "b@example.com", "path-1", "FAILED_RETRY_PENDING");
    retrying.setRetryAttempts(1);
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(queue.findByCampaignId("c1")).thenReturn(List.of(failed, retrying));
    when(queue.save(any())).thenAnswer(inv -> inv.getArgument(0));

    CampaignEntity out = service.retryFailed("c1");

    assertEquals(CampaignStatus.QUEUED, out.getStatus());
    assertEquals("PENDING", failed.getStatus());
    assertEquals(0, failed.getRetryAttempts());
    assertEquals("FAILED_RETRY_PENDING", retrying.getStatus());
    assertEquals(1, retrying.getRetryAttempts());
  }

  @Test
  void enqueueTracksFirstListId() {
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
    List<SendQueueEntity> enqueued = new ArrayList<>();
    when(queue.save(any()))
        .thenAnswer(
            inv -> {
              enqueued.add(inv.getArgument(0));
              return inv.getArgument(0);
            });
    when(queue.findByCampaignId("c1")).thenAnswer(inv -> List.copyOf(enqueued));

    service.queueAndSend("c1");

    assertEquals(1, enqueued.size());
    assertEquals("list-a", enqueued.get(0).getListId());
  }
}
