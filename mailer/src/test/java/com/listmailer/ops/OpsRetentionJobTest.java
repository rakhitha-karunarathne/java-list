package com.listmailer.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
class OpsRetentionJobTest {

  @Mock OpsLogRepository opsLogs;
  @Mock ParametersPort parameters;
  @InjectMocks OpsRetentionJob job;

  @Test
  void purgeUsesParameterDays() {
    when(parameters.get(ParameterKeys.OPS_LOG_RETENTION_DAYS)).thenReturn(Optional.of("7"));
    when(opsLogs.deleteOlderThan(any())).thenReturn(3);

    assertEquals(3, job.purge());
    verify(opsLogs).deleteOlderThan(any());
  }
}
