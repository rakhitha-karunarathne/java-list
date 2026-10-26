package com.listmailer.mts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignSendService;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.catalogs.FooterRepository;
import com.listmailer.lists.ListRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MtsCampaignFactoryTest {

  @Mock CampaignRepository campaigns;
  @Mock CampaignListRepository campaignLists;
  @Mock CampaignSendService sendService;
  @Mock FooterRepository footers;
  @Mock ListRepository lists;
  @InjectMocks MtsCampaignFactory factory;

  @Test
  void createQueuedSkipsLeavingDraft() {
    when(campaigns.save(any()))
        .thenAnswer(
            inv -> {
              CampaignEntity c = inv.getArgument(0);
              assertEquals(CampaignStatus.DRAFT, c.getStatus());
              return c;
            });
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));
    CampaignEntity queued =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.QUEUED, Instant.now());
    when(sendService.queueAndSend(anyString())).thenReturn(queued);
    MtsMappingEntity map =
        new MtsMappingEntity("m1", "i1", "map", null, null, Instant.now());

    CampaignEntity out = factory.createQueued("Hi", "<p>x</p>", "x", map, List.of("list-a"));

    assertEquals(CampaignStatus.QUEUED, out.getStatus());
    verify(sendService).queueAndSend(anyString());
  }
}
