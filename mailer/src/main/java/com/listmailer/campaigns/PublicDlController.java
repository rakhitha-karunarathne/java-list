package com.listmailer.campaigns;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class PublicDlController {

  private final CampaignRepository campaigns;
  private final CampaignAttachmentRepository attachments;

  public PublicDlController(
      CampaignRepository campaigns, CampaignAttachmentRepository attachments) {
    this.campaigns = campaigns;
    this.attachments = attachments;
  }

  @GetMapping("/dl/{campaignId}/{filename}")
  public ResponseEntity<byte[]> download(
      @PathVariable String campaignId, @PathVariable String filename) {
    if (!campaigns.existsById(campaignId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    CampaignAttachmentEntity blob =
        attachments
            .findByCampaignIdAndFilename(campaignId, filename)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    // Public surface is for campaign images only; non-image attachments stay SMTP-only.
    if (blob.getContentType() == null || !blob.getContentType().toLowerCase().startsWith("image/")) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(blob.getContentType()));
    headers.setContentLength(blob.getContent().length);
    headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");
    return new ResponseEntity<>(blob.getContent(), headers, HttpStatus.OK);
  }
}
