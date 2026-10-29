package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CampaignPauseTest {

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
  @Mock ParametersPort parameters;
  @InjectMocks CampaignsController controller;

  @BeforeEach
  void stubs() {
    lenient()
        .when(campaignLists.findByCampaignIdOrderByPositionAscListIdAsc("c1"))
        .thenReturn(List.of());
    lenient().when(queue.findByCampaignId("c1")).thenReturn(List.of());
    lenient().when(opens.findByCampaignId("c1")).thenReturn(List.of());
    lenient().when(attachments.findByCampaignId("c1")).thenReturn(List.of());
    lenient().when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void pauseQueuedThenResumeToQueued() {
    CampaignEntity c =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.QUEUED, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(c));

    assertEquals("PAUSED", controller.pause("c1").status());
    verify(pauseCache).clearCampaign("c1");

    assertEquals("QUEUED", controller.resume("c1").status());
  }

  @Test
  void pauseRejectedFromDraft() {
    CampaignEntity c =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(c));

    assertThrows(ResponseStatusException.class, () -> controller.pause("c1"));
  }
}
