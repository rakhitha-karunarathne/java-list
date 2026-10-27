package com.listmailer.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class ActivityLogRetentionJobTest {

  @Mock ActivityLogRepository logs;
  @Mock ParametersPort parameters;
  @InjectMocks ActivityLogRetentionJob job;

  @Test
  void purgesEachKindUsingItsParameter() {
    when(parameters.get(ParameterKeys.BOUNCE_PROCESS_LOG_RETENTION_DAYS)).thenReturn(Optional.of("90"));
    when(parameters.get(ParameterKeys.SEND_QUEUE_LOG_RETENTION_DAYS)).thenReturn(Optional.of("90"));
    when(parameters.get(ParameterKeys.MTS_PROCESS_LOG_RETENTION_DAYS)).thenReturn(Optional.of("90"));
    when(logs.deleteKindOlderThan(any(), any())).thenReturn(1);

    assertEquals(3, job.purge());
    verify(logs).deleteKindOlderThan(eq(ActivityLogService.BOUNCE), any(Instant.class));
    verify(logs).deleteKindOlderThan(eq(ActivityLogService.SEND), any(Instant.class));
    verify(logs).deleteKindOlderThan(eq(ActivityLogService.MTS), any(Instant.class));
  }
}
