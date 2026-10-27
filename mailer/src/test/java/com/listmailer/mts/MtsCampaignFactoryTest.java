package com.listmailer.mts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.CampaignAttachmentRepository;
import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignSendService;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.FooterRepository;
import com.listmailer.catalogs.SendingPathRepository;
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
  @Mock CampaignAttachmentRepository attachments;
  @Mock CampaignSendService sendService;
  @Mock FooterRepository footers;
  @Mock ListRepository lists;
  @Mock SendingPathRepository sendingPaths;
  @Mock CatalogReferenceService references;
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
    var grant = new MtsSenderListGrantEntity("g1", "i1", "a@x.com", "list-a", null, 0);

    CampaignEntity out = factory.createQueued("Hi", "<p>x</p>", "x", List.of(grant));

    assertEquals(CampaignStatus.QUEUED, out.getStatus());
    verify(sendService).queueAndSend(anyString());
  }

  @Test
  void createWithoutAutoQueueStaysDraft() {
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));
    var grant = new MtsSenderListGrantEntity("g1", "i1", "a@x.com", "list-a", null, 0);

    CampaignEntity out = factory.create("Hi", "<p>x</p>", "x", List.of(grant), false);

    assertEquals(CampaignStatus.DRAFT, out.getStatus());
    verify(sendService, never()).queueAndSend(anyString());
  }

  @Test
  void createWithoutSenderPathLeavesCampaignPathNull() {
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));
    var grant = new MtsSenderListGrantEntity("g1", "i1", "a@x.com", "list-a", null, 0);

    CampaignEntity out =
        factory.create("Hi", "<p>x</p>", "x", List.of(grant), false, List.of(), null);

    assertNull(out.getSendingPathId());
    verify(sendingPaths, never()).existsById(anyString());
    verify(references, never()).add(any(), anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void createPersistsAttachments() {
    when(campaigns.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(attachments.save(any())).thenAnswer(inv -> inv.getArgument(0));
    var grant = new MtsSenderListGrantEntity("g1", "i1", "a@x.com", "list-a", null, 0);
    var file =
        new com.listmailer.mailbox.MimeBodyParser.Attachment(
            "Receipt_562459.pdf", "application/pdf", new byte[] {1, 2, 3});

    factory.create("Hi", "<p>x</p>", "x", List.of(grant), false, List.of(file));

    verify(attachments).save(any());
    verify(sendService, never()).queueAndSend(anyString());
  }

  @Test
  void createKeepsExternalImageLinksInHtmlAndStoresMultipartImage() {
    when(campaigns.save(any()))
        .thenAnswer(
            inv -> {
              CampaignEntity c = inv.getArgument(0);
              assertTrue(c.getHtmlBody().contains("src=\"https://cdn.example.com/banner.png\""));
              assertTrue(c.getHtmlBody().contains("src=\"logo.png\""));
              assertFalse(c.getHtmlBody().contains("cid:"));
              return c;
            });
    when(campaignLists.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(attachments.save(any())).thenAnswer(inv -> inv.getArgument(0));
    var grant = new MtsSenderListGrantEntity("g1", "i1", "a@x.com", "list-a", null, 0);
    String html =
        "<p><img src=\"logo.png\"/><img src=\"https://cdn.example.com/banner.png\"/></p>";
    var inline =
        new com.listmailer.mailbox.MimeBodyParser.Attachment(
            "logo.png", "image/png", new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47});

    factory.create("Images", html, "Images", List.of(grant), false, List.of(inline));

    verify(attachments).save(any());
    verify(sendService, never()).queueAndSend(anyString());
  }
}
