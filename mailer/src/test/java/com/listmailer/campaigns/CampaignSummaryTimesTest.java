package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampaignSummaryTimesTest {

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
  void indexReturnsCreatedAndQueuedNewestFirst() {
    Instant older = Instant.parse("2026-01-01T10:00:00Z");
    Instant newer = Instant.parse("2026-01-02T10:00:00Z");
    Instant queued = Instant.parse("2026-01-02T11:00:00Z");
    CampaignEntity draft = new CampaignEntity("c-new", "New", "<p>x</p>", "x", CampaignStatus.DRAFT, newer);
    CampaignEntity finished =
        new CampaignEntity("c-old", "Old", "<p>x</p>", "x", CampaignStatus.FINISHED, older);
    finished.setQueuedAt(queued);
    when(campaigns.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(draft, finished));

    List<CampaignsController.CampaignSummary> rows = controller.index();

    verify(campaigns).findAllByOrderByCreatedAtDesc();
    assertEquals(2, rows.size());
    assertEquals("c-new", rows.get(0).id());
    assertEquals(newer, rows.get(0).createdAt());
    assertNull(rows.get(0).queuedAt());
    assertEquals("c-old", rows.get(1).id());
    assertEquals(older, rows.get(1).createdAt());
    assertEquals(queued, rows.get(1).queuedAt());
  }
}
