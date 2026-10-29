package com.listmailer.mts;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.CampaignAttachmentRepository;
import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignSendService;
import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.FooterRepository;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.lists.ListRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class MtsEmbedImagesCopyTest {

  @Test
  void createCopiesEmbedFlagOntoCampaign() {
    CampaignRepository campaigns = mock(CampaignRepository.class);
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MtsCampaignFactory factory =
        new MtsCampaignFactory(
            campaigns,
            mock(CampaignListRepository.class),
            mock(CampaignAttachmentRepository.class),
            mock(CampaignSendService.class),
            mock(FooterRepository.class),
            mock(ListRepository.class),
            mock(SendingPathRepository.class),
            mock(CatalogReferenceService.class));

    MtsSenderListGrantEntity grant =
        new MtsSenderListGrantEntity("g1", "inbox", "a@x.com", "list1", null, 0);
    ArgumentCaptor<CampaignEntity> cap = ArgumentCaptor.forClass(CampaignEntity.class);

    factory.create("Sub", "<p>x</p>", "x", List.of(grant), false, List.of(), null, false);
    verify(campaigns, Mockito.atLeastOnce()).save(cap.capture());
    assertFalse(cap.getValue().isEmbedImages());

    Mockito.reset(campaigns);
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    factory.create("Sub", "<p>x</p>", "x", List.of(grant), false, List.of(), null, true);
    verify(campaigns, Mockito.atLeastOnce()).save(cap.capture());
    assertTrue(cap.getValue().isEmbedImages());
  }
}
