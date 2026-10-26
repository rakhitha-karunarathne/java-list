package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
class PublicDlControllerTest {

  @Mock CampaignRepository campaigns;
  @Mock CampaignAttachmentRepository attachments;
  @InjectMocks PublicDlController controller;

  @Test
  void servesImageBlob() {
    when(campaigns.existsById("c1")).thenReturn(true);
    when(attachments.findByCampaignIdAndFilename("c1", "logo.png"))
        .thenReturn(
            Optional.of(
                new CampaignAttachmentEntity(
                    "a1", "c1", "logo.png", "image/png", new byte[] {1, 2})));
    ResponseEntity<byte[]> res = controller.download("c1", "logo.png");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals(2, res.getBody().length);
  }

  @Test
  void hidesNonImageAttachments() {
    when(campaigns.existsById("c1")).thenReturn(true);
    when(attachments.findByCampaignIdAndFilename("c1", "note.txt"))
        .thenReturn(
            Optional.of(
                new CampaignAttachmentEntity(
                    "a1", "c1", "note.txt", "text/plain", new byte[] {1})));
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.download("c1", "note.txt"));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void rejectsPathTraversalFilename() {
    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> controller.download("c1", "../secret.png"));
    assertEquals(404, ex.getStatusCode().value());
  }
}
