package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListEntity;
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

@ExtendWith(MockitoExtension.class)
class CampaignDetailStatsTest {

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
  void reportsListsSendWindowBouncesAndRecipients() {
    Instant started = Instant.parse("2026-10-26T10:00:00Z");
    Instant opened = Instant.parse("2026-10-26T10:05:00Z");
    CampaignEntity campaign =
        new CampaignEntity("c1", "Hello", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now());
    SendQueueEntity sent = new SendQueueEntity("q1", "c1", "m1", "b@x.com", "path", "SENT");
    sent.setSentAt(started);
    SendQueueEntity waiting = new SendQueueEntity("q2", "c1", "m2", "a@x.com", "path", "PENDING");
    when(campaigns.findById("c1")).thenReturn(Optional.of(campaign));
    when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of(new CampaignListEntity("c1", "l1", 0)));
    when(lists.findById("l1"))
        .thenReturn(
            Optional.of(new ListEntity("l1", "News", "path", null, null, null, null, Instant.now())));
    when(queue.findByCampaignId("c1")).thenReturn(List.of(sent, waiting));
    when(bounces.countBySendQueueIdIn(any())).thenReturn(0L);
    when(bounces.findBySendQueueIdIn(any())).thenReturn(List.of());
    when(opens.findByCampaignId("c1"))
        .thenReturn(
            List.of(
                new CampaignOpenEntity("o1", "c1", "m1", "q1", opened),
                new CampaignOpenEntity("o2", "c1", "m1", "q1", opened.plusSeconds(30))));
    when(attachments.findByCampaignId("c1")).thenReturn(List.of());

    CampaignsController.CampaignDetail detail = controller.get("c1");

    assertEquals("News", detail.lists().get(0).name());
    assertEquals(started, detail.sendStartedAt());
    assertEquals(started, detail.sendFinishedAt());
    assertEquals(1, detail.sentCount());
    assertEquals(1, detail.waitingCount());
    assertEquals(0, detail.bounceCount());
    assertEquals(1, detail.uniqueViews());
    assertEquals(2, detail.totalViews());
    assertEquals(2, detail.recipients().size());
    assertEquals("a@x.com", detail.recipients().get(0).email());
    assertEquals("Pending", detail.recipients().get(0).status());
    assertEquals("b@x.com", detail.recipients().get(1).email());
    assertEquals("Opened", detail.recipients().get(1).status());
    assertEquals(opened, detail.recipients().get(1).firstOpenAt());
    assertEquals(2, detail.recipients().get(1).openCount());
  }

  @Test
  void bouncedWinsOverOpened() {
    assertEquals("Bounced", CampaignsController.recipientStatus("SENT", true, true));
    assertEquals("Opened", CampaignsController.recipientStatus("SENT", false, true));
    assertEquals("Sent", CampaignsController.recipientStatus("SENT", false, false));
    assertEquals("Pending", CampaignsController.recipientStatus("PENDING", false, false));
    assertEquals(
        "Failed-Retry Pending",
        CampaignsController.recipientStatus("FAILED_RETRY_PENDING", false, false));
    assertEquals("Send Failed", CampaignsController.recipientStatus("SEND_FAILED", false, false));
  }
}
