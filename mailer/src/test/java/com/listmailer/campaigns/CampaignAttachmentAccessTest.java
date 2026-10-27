package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class CampaignAttachmentAccessTest {

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
  void downloadReturnsBytesWithDisposition() {
    CampaignEntity c =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now());
    byte[] bytes = {9, 8, 7};
    when(campaigns.findById("c1")).thenReturn(Optional.of(c));
    when(attachments.findByIdAndCampaignId("a1", "c1"))
        .thenReturn(
            Optional.of(
                new CampaignAttachmentEntity("a1", "c1", "note.txt", "text/plain", bytes)));

    ResponseEntity<byte[]> res = controller.downloadAttachment("c1", "a1");

    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertArrayEquals(bytes, res.getBody());
    assertEquals("text/plain", res.getHeaders().getContentType().toString());
    assertEquals("attachment", res.getHeaders().getContentDisposition().getType());
    assertEquals("note.txt", res.getHeaders().getContentDisposition().getFilename());
  }

  @Test
  void deleteAttachmentAllowedOnDraftOnly() {
    CampaignEntity draft =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.DRAFT, Instant.now());
    CampaignAttachmentEntity blob =
        new CampaignAttachmentEntity("a1", "c1", "note.txt", "text/plain", new byte[] {1});
    when(campaigns.findById("c1")).thenReturn(Optional.of(draft));
    when(attachments.findByIdAndCampaignId("a1", "c1")).thenReturn(Optional.of(blob));

    controller.deleteAttachment("c1", "a1");

    verify(attachments).delete(blob);
  }

  @Test
  void deleteAttachmentRejectedWhenNotDraft() {
    CampaignEntity finished =
        new CampaignEntity("c1", "Hi", "<p>x</p>", "x", CampaignStatus.FINISHED, Instant.now());
    when(campaigns.findById("c1")).thenReturn(Optional.of(finished));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.deleteAttachment("c1", "a1"));
    assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    verify(attachments, never()).delete(org.mockito.ArgumentMatchers.any());
  }
}
