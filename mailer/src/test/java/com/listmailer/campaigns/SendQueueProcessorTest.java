package com.listmailer.campaigns;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendQueueProcessorTest {

  @Mock CampaignSendService sendService;
  @Mock SendQueueRepository queue;
  @Mock ParametersPort parameters;
  @Mock SendPauseCache pauseCache;
  @InjectMocks SendQueueProcessor processor;

  @Test
  void sendsOnlyTheHourlyRoom() {
    when(parameters.get(ParameterKeys.SEND_BATCH_SIZE)).thenReturn(Optional.of("600"));
    when(parameters.get(ParameterKeys.SEND_BATCH_DURATION_MINUTES)).thenReturn(Optional.of("10"));
    when(parameters.get(ParameterKeys.SEND_MESSAGES_PER_HOUR)).thenReturn(Optional.of("3600"));
    when(parameters.get(ParameterKeys.SEND_DELAY_PER_EMAIL_MS)).thenReturn(Optional.of("0"));
    when(queue.countByStatusAndSentAtAfter(eq("SENT"), any(Instant.class))).thenReturn(3550L);

    processor.tick();

    verify(sendService).processPendingBatch(eq(50), eq(0L), any(Instant.class));
  }

  @Test
  void sendsNothingWhenTheHourIsFull() {
    when(parameters.get(ParameterKeys.SEND_BATCH_SIZE)).thenReturn(Optional.of("600"));
    when(parameters.get(ParameterKeys.SEND_BATCH_DURATION_MINUTES)).thenReturn(Optional.of("10"));
    when(parameters.get(ParameterKeys.SEND_MESSAGES_PER_HOUR)).thenReturn(Optional.of("3600"));
    when(parameters.get(ParameterKeys.SEND_DELAY_PER_EMAIL_MS)).thenReturn(Optional.of("1000"));
    when(queue.countByStatusAndSentAtAfter(eq("SENT"), any(Instant.class))).thenReturn(3600L);

    processor.tick();

    verify(sendService, never()).processPendingBatch(org.mockito.ArgumentMatchers.anyInt(), anyLong(), any());
  }
}
