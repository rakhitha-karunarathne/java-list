package com.listmailer.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
  void purgesEachKindByAgeAndCount() {
    when(parameters.get(ParameterKeys.BOUNCE_PROCESS_LOG_RETENTION_DAYS)).thenReturn(Optional.of("30"));
    when(parameters.get(ParameterKeys.SEND_QUEUE_LOG_RETENTION_DAYS)).thenReturn(Optional.of("30"));
    when(parameters.get(ParameterKeys.MTS_PROCESS_LOG_RETENTION_DAYS)).thenReturn(Optional.of("30"));
    when(parameters.get(ParameterKeys.BOUNCE_PROCESS_LOG_MAX_ROWS)).thenReturn(Optional.of("5000"));
    when(parameters.get(ParameterKeys.SEND_QUEUE_LOG_MAX_ROWS)).thenReturn(Optional.of("5000"));
    when(parameters.get(ParameterKeys.MTS_PROCESS_LOG_MAX_ROWS)).thenReturn(Optional.of("5000"));
    when(logs.deleteKindOlderThan(any(), any())).thenReturn(1);
    when(logs.deleteKindBeyondNewest(any(), anyInt())).thenReturn(1);

    assertEquals(6, job.purge());
    verify(logs).deleteKindOlderThan(eq(ActivityLogService.BOUNCE), any(Instant.class));
    verify(logs).deleteKindBeyondNewest(eq(ActivityLogService.BOUNCE), eq(5000));
    verify(logs).deleteKindOlderThan(eq(ActivityLogService.SEND), any(Instant.class));
    verify(logs).deleteKindBeyondNewest(eq(ActivityLogService.SEND), eq(5000));
    verify(logs).deleteKindOlderThan(eq(ActivityLogService.MTS), any(Instant.class));
    verify(logs).deleteKindBeyondNewest(eq(ActivityLogService.MTS), eq(5000));
  }
}
