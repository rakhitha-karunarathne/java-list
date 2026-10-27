package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.members.MemberRepository;
import com.listmailer.ops.ActivityLogService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BounceProcessAsyncTest {

  @Mock BounceEventRepository events;
  @Mock SendQueueRepository queue;
  @Mock MemberRepository members;
  @Mock ParametersPort parameters;
  @Mock OpsHooksPort ops;
  @Mock ActivityLogService activity;

  @Test
  void secondStartRejectedWhileRunning() throws Exception {
    CountDownLatch started = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    when(events.findIdsByStatus("CORRELATED")).thenReturn(List.of("b1"));
    when(events.findByStatus("CORRELATED"))
        .thenAnswer(
            inv -> {
              started.countDown();
              release.await(5, TimeUnit.SECONDS);
              return List.of();
            });
    BounceProcessService process =
        new BounceProcessService(events, queue, members, parameters, ops, activity);

    assertEquals(BounceProcessService.StartOutcome.STARTED, process.startProcessAsync());
    assertTrue(started.await(5, TimeUnit.SECONDS));
    assertEquals(BounceProcessService.StartOutcome.ALREADY_RUNNING, process.startProcessAsync());
    release.countDown();
  }

  @Test
  void skippedWhenNoCorrelatedThenCanStart() {
    when(events.findIdsByStatus("CORRELATED"))
        .thenReturn(List.of(), List.of("b1"), List.of("b1"), List.of("b1"), List.of("b1"));
    lenient().when(events.findByStatus("CORRELATED")).thenReturn(List.of());
    BounceProcessService process =
        new BounceProcessService(events, queue, members, parameters, ops, activity);
    assertEquals(BounceProcessService.StartOutcome.SKIPPED, process.startProcessAsync());
    assertEquals(BounceProcessService.StartOutcome.STARTED, process.startProcessAsync());
  }
}
