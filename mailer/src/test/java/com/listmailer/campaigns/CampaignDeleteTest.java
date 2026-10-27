package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
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
class CampaignDeleteTest {

  @Mock CampaignRepository campaigns;
  @Mock CampaignListRepository campaignLists;
  @Mock CampaignAttachmentRepository attachments;
  @Mock ListRepository lists;
  @Mock ListAccessService access;
  @Mock CampaignSendService sendService;
  @Mock SendQueueRepository queue;
  @Mock CampaignOpenRepository opens;
  @Mock BounceEventRepository bounces;
  @Mock OpsHooksPort ops;
  @Mock SendPauseCache pauseCache;
  @InjectMocks CampaignsController controller;

  @Test
  void deleteFinishedClearsBouncesOpensQueueListsAttachmentsThenCampaign() {
    CampaignEntity c =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now());
    SendQueueEntity row = new SendQueueEntity("q1", "c1", "m1", "a@x.com", "path", "SENT");
    when(campaigns.findById("c1")).thenReturn(Optional.of(c));
    when(queue.findByCampaignId("c1")).thenReturn(List.of(row));
    when(access.currentUser()).thenReturn(Optional.empty());

    controller.delete("c1");

    verify(bounces).deleteBySendQueueIdIn(List.of("q1"));
    verify(opens).deleteByCampaignId("c1");
    verify(attachments).deleteByCampaignId("c1");
    verify(queue).deleteByCampaignId("c1");
    verify(campaignLists).deleteByCampaignId("c1");
    verify(campaigns).delete(c);
    verify(ops)
        .audit(eq("campaign.delete"), eq("campaign=c1"), eq("system"), isNull(), isNull());
  }

  @Test
  void deleteDraftWithNoQueueSkipsBounceDelete() {
    CampaignEntity c =
        new CampaignEntity("c2", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c2")).thenReturn(Optional.of(c));
    when(queue.findByCampaignId("c2")).thenReturn(List.of());
    when(access.currentUser()).thenReturn(Optional.empty());

    controller.delete("c2");

    verify(bounces, never()).deleteBySendQueueIdIn(any());
    verify(opens).deleteByCampaignId("c2");
    verify(campaigns).delete(c);
  }

  @Test
  void deleteRejectsQueuedSendingAndPaused() {
    for (CampaignStatus status :
        List.of(CampaignStatus.QUEUED, CampaignStatus.SENDING, CampaignStatus.PAUSED)) {
      CampaignEntity c =
          new CampaignEntity("c-" + status, "Hi", "<p>x</p>", "x", status, Instant.now());
      when(campaigns.findById(c.getId())).thenReturn(Optional.of(c));
      assertThrows(ResponseStatusException.class, () -> controller.delete(c.getId()));
    }
    verify(campaigns, never()).delete(any());
  }
}
