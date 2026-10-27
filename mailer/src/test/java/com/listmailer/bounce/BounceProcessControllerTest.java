package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class BounceProcessControllerTest {

  @Mock BounceProcessService process;
  @InjectMocks BounceProcessController controller;

  @Test
  void skippedMessage() {
    when(process.startProcessAsync()).thenReturn(BounceProcessService.StartOutcome.SKIPPED);
    assertEquals(
        "skipped — no new bounces since last run", controller.runNow().get("message"));
  }

  @Test
  void startedMessage() {
    when(process.startProcessAsync()).thenReturn(BounceProcessService.StartOutcome.STARTED);
    assertEquals("process started", controller.runNow().get("message"));
  }

  @Test
  void alreadyRunningConflict() {
    when(process.startProcessAsync())
        .thenReturn(BounceProcessService.StartOutcome.ALREADY_RUNNING);
    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.runNow());
    assertEquals(409, ex.getStatusCode().value());
  }
}
