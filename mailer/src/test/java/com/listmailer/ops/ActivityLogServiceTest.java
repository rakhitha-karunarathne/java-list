package com.listmailer.ops;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
class ActivityLogServiceTest {

  @Mock ActivityLogRepository logs;
  @Mock ParametersPort parameters;
  @InjectMocks ActivityLogService service;

  @Test
  void skipsWriteWhenDisabled() {
    when(parameters.get(ParameterKeys.BOUNCE_PROCESS_LOG_ENABLED)).thenReturn(Optional.of("false"));

    service.write(ActivityLogService.BOUNCE, "START", "x");

    verify(logs, never()).save(any());
  }

  @Test
  void writesWhenEnabledOrUnset() {
    when(parameters.get(ParameterKeys.SEND_QUEUE_LOG_ENABLED)).thenReturn(Optional.of("true"));
    when(parameters.get(ParameterKeys.MTS_PROCESS_LOG_ENABLED)).thenReturn(Optional.empty());
    when(logs.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.write(ActivityLogService.SEND, "SEND", "a");
    service.write(ActivityLogService.MTS, "FETCH", "b");

    verify(logs, org.mockito.Mockito.times(2)).save(any());
  }
}
