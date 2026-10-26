package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenPixelController {

  /** 1x1 transparent GIF */
  private static final byte[] PIXEL =
      new byte[] {
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00, (byte) 0x80, 0x00, 0x00, (byte)
            0xff, (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x00, 0x21, (byte) 0xf9, 0x04, 0x01, 0x00,
        0x00, 0x00, 0x00, 0x2c, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x02,
        0x44, 0x01, 0x00, 0x3b
      };

  private final SendQueueRepository queue;
  private final CampaignOpenRepository opens;

  public OpenPixelController(SendQueueRepository queue, CampaignOpenRepository opens) {
    this.queue = queue;
    this.opens = opens;
  }

  @GetMapping("/t/{token}")
  public ResponseEntity<byte[]> pixel(@PathVariable String token) {
    // Return GIF immediately; persist open off the request thread.
    CompletableFuture.runAsync(() -> recordOpen(token));
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_GIF);
    headers.setCacheControl("no-store");
    return new ResponseEntity<>(PIXEL, headers, HttpStatus.OK);
  }

  private void recordOpen(String token) {
    queue
        .findByPixelToken(token)
        .ifPresent(
            row ->
                opens.save(
                    new CampaignOpenEntity(
                        Guids.newGuid(),
                        row.getCampaignId(),
                        row.getMemberId(),
                        row.getId(),
                        Instant.now())));
  }
}
