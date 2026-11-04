package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BounceEventsRetentionJobTest {

  @Mock BounceEventRepository events;
  @Mock ParametersPort parameters;
  @InjectMocks BounceEventsRetentionJob job;

  @Test
  void purgeDeletesProcessedAndUncorrelatedWithConfiguredDays() {
    when(parameters.get(ParameterKeys.BOUNCE_MESSAGES_PROCESSED_RETENTION_DAYS))
        .thenReturn(Optional.of("45"));
    when(parameters.get(ParameterKeys.BOUNCE_MESSAGES_UNCORRELATED_RETENTION_DAYS))
        .thenReturn(Optional.of("90"));
    when(events.deleteByStatusAndReceivedAtBefore(eq("PROCESSED"), any())).thenReturn(3);
    when(events.deleteByStatusAndReceivedAtBefore(eq("UNCORRELATED"), any())).thenReturn(2);

    assertEquals(5, job.purge());

    verify(events).deleteByStatusAndReceivedAtBefore(eq("PROCESSED"), any());
    verify(events).deleteByStatusAndReceivedAtBefore(eq("UNCORRELATED"), any());
  }

  @Test
  void purgeFallsBackToSixtyWhenParamMissingOrInvalid() {
    when(parameters.get(ParameterKeys.BOUNCE_MESSAGES_PROCESSED_RETENTION_DAYS))
        .thenReturn(Optional.empty());
    when(parameters.get(ParameterKeys.BOUNCE_MESSAGES_UNCORRELATED_RETENTION_DAYS))
        .thenReturn(Optional.of("0"));
    when(events.deleteByStatusAndReceivedAtBefore(eq("PROCESSED"), any())).thenReturn(1);
    when(events.deleteByStatusAndReceivedAtBefore(eq("UNCORRELATED"), any())).thenReturn(0);

    assertEquals(1, job.purge());

    verify(events).deleteByStatusAndReceivedAtBefore(eq("PROCESSED"), any());
    verify(events).deleteByStatusAndReceivedAtBefore(eq("UNCORRELATED"), any());
  }
}
