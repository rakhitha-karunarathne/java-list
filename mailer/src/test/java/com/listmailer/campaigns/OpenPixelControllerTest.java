package com.listmailer.campaigns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class OpenPixelControllerTest {

  @Mock SendQueueRepository queue;
  @Mock CampaignOpenRepository opens;
  @InjectMocks OpenPixelController controller;

  @Test
  void returnsGifAndRecordsOpenAsync() {
    SendQueueEntity row =
        new SendQueueEntity("q1", "c1", "m1", "a@b.com", "path", "SENT");
    row.setPixelToken("tok-1");
    when(queue.findByPixelToken("tok-1")).thenReturn(Optional.of(row));
    when(opens.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ResponseEntity<byte[]> res = controller.pixel("tok-1");
    assertEquals(HttpStatus.OK, res.getStatusCode());
    assertEquals("image/gif", res.getHeaders().getContentType().toString());
    verify(opens, timeout(2000)).save(any());
  }
}
