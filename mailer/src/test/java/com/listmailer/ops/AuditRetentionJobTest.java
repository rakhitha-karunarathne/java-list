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
class AuditRetentionJobTest {

  @Mock AuditLogRepository audit;
  @Mock ParametersPort parameters;
  @InjectMocks AuditRetentionJob job;

  @Test
  void purgeUsesAuditRetentionParameter() {
    when(parameters.get(ParameterKeys.AUDIT_RETENTION_DAYS)).thenReturn(Optional.of("30"));
    when(audit.deleteOlderThan(any())).thenReturn(2);
    assertEquals(2, job.purge());
    verify(audit).deleteOlderThan(any());
  }
}
