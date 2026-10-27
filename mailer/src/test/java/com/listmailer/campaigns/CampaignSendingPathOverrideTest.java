package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.kernel.token.OpaqueTokenHelper;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import com.listmailer.ops.ActivityLogService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignSendingPathOverrideTest {

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
  @Mock ActivityLogService activity;
  @Mock SendPauseCache pauseCache;
  @Mock SendingPathRepository sendingPaths;

  CampaignSendService service;

  @BeforeEach
  void setUp() {
    service =
        new CampaignSendService(
            campaigns,
            campaignLists,
            attachments,
            queue,
            lists,
            listMembers,
            members,
            smtp,
            ops,
            parameters,
            htmlRewriter,
            imageFetch,
            opaqueTokens,
            sendingPaths,
            activity,
            null,
            pauseCache);
  }

  @Test
  void queueUsesCampaignPathWhenSet() {
    CampaignEntity campaign =
        new CampaignEntity(
            "c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    campaign.setSendingPathId("path-campaign");
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(new CampaignListEntity("c1", "L1", 0)));
    ListEntity list =
        new ListEntity("L1", "News", "path-list", null, null, null, null, Instant.now());
    when(lists.findById("L1")).thenReturn(Optional.of(list));
    when(sendingPaths.existsById("path-campaign")).thenReturn(true);
    when(parameters.get(ParameterKeys.IMAGE_FETCH_MODE)).thenReturn(Optional.of("Off"));
    when(imageFetch.ingest(eq("c1"), any(), any(), any())).thenReturn("<p>x</p>");
    when(listMembers.findByListId("L1"))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", false, Instant.now())));
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(queue.findByCampaignId("c1")).thenReturn(List.of());
    when(queue.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.queueAndSend("c1");

    ArgumentCaptor<SendQueueEntity> cap = ArgumentCaptor.forClass(SendQueueEntity.class);
    verify(queue).save(cap.capture());
    assertEquals("path-campaign", cap.getValue().getSendingPathId());
  }

  @Test
  void queueFallsBackToFirstListPath() {
    CampaignEntity campaign =
        new CampaignEntity(
            "c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(new CampaignListEntity("c1", "L1", 0)));
    ListEntity list =
        new ListEntity("L1", "News", "path-list", null, null, null, null, Instant.now());
    when(lists.findById("L1")).thenReturn(Optional.of(list));
    when(parameters.get(ParameterKeys.IMAGE_FETCH_MODE)).thenReturn(Optional.of("Off"));
    when(imageFetch.ingest(eq("c1"), any(), any(), any())).thenReturn("<p>x</p>");
    when(listMembers.findByListId("L1"))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", false, Instant.now())));
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    when(queue.findByCampaignId("c1")).thenReturn(List.of());
    when(queue.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.queueAndSend("c1");

    ArgumentCaptor<SendQueueEntity> cap = ArgumentCaptor.forClass(SendQueueEntity.class);
    verify(queue).save(cap.capture());
    assertEquals("path-list", cap.getValue().getSendingPathId());
    verify(sendingPaths, never()).existsById(any());
  }
}
