package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import com.listmailer.ops.ActivityLogService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BounceProcessServiceTest {

  @Mock BounceEventRepository events;
  @Mock SendQueueRepository queue;
  @Mock MemberRepository members;
  @Mock ParametersPort parameters;
  @Mock OpsHooksPort ops;
  @Mock com.listmailer.ops.ActivityLogService activity;
  @InjectMocks BounceProcessService service;

  @BeforeEach
  void limits() {
    lenient().when(parameters.get(ParameterKeys.BOUNCE_HARD_LIMIT)).thenReturn(Optional.of("3"));
    lenient().when(parameters.get(ParameterKeys.BOUNCE_SOFT_LIMIT)).thenReturn(Optional.of("6"));
    lenient()
        .when(parameters.get(ParameterKeys.BOUNCE_HOLDBACK_MULTIPLIER))
        .thenReturn(Optional.of("2"));
  }

  @Test
  void threeHardDisablesWithoutTouchingUnsubSemantics() {
    List<SendQueueEntity> sends = new ArrayList<>();
    List<BounceEventEntity> bounces = new ArrayList<>();
    Instant t = Instant.parse("2026-01-01T00:00:00Z");
    for (int i = 0; i < 3; i++) {
      SendQueueEntity s = new SendQueueEntity("q" + i, "c", "m1", "a@x.com", "p", "SENT");
      s.setSentAt(t.plusSeconds(i));
      sends.add(s);
      BounceEventEntity b =
          new BounceEventEntity(
              "b" + i, "mb", t, null, "x", "f", "", new byte[0], "CORRELATED");
      b.setMemberId("m1");
      b.setSendQueueId("q" + i);
      b.setClassification("HARD");
      bounces.add(b);
    }
    when(events.findIdsByStatus("CORRELATED"))
        .thenReturn(
            List.of("b0", "b1", "b2"),
            List.of("b0", "b1", "b2"),
            List.of("b0", "b1", "b2"),
            List.of());
    when(events.findByStatus("CORRELATED")).thenReturn(bounces);
    when(events.findByStatusAndMemberId("CORRELATED", "m1")).thenReturn(bounces);
    when(queue.findByMemberIdAndStatus("m1", "SENT")).thenReturn(sends);
    MemberEntity m = new MemberEntity("m1", "a@x.com", true, Instant.now());
    when(members.findById("m1")).thenReturn(Optional.of(m));
    when(members.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(events.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertEquals(1, service.processDaily());
    assertFalse(m.isEnabled());
    assertTrue(m.getDisableReason().contains("hard"));
    assertTrue(m.getDisableReason().contains("bounce"));
    assertFalse(m.getDisableReason().toLowerCase().contains("unsubscribe"));
  }

  @Test
  void emptyCorrelatedSkipsWithoutLog() {
    when(events.findIdsByStatus("CORRELATED")).thenReturn(List.of());
    assertEquals(0, service.processDaily());
    verify(activity, never()).write(anyString(), anyString(), anyString());
    verify(events, never()).findByStatus("CORRELATED");
  }

  @Test
  void skipsWhenCorrelatedSetUnchanged() {
    when(events.findIdsByStatus("CORRELATED")).thenReturn(List.of("b0"));
    when(events.findByStatus("CORRELATED")).thenReturn(List.of());
    assertEquals(0, service.processDaily());
    assertEquals(0, service.processDaily());
    verify(events, times(1)).findByStatus("CORRELATED");
    verify(activity, times(1)).write(eq(ActivityLogService.BOUNCE), eq("START"), anyString());
    verify(activity, times(1)).write(eq(ActivityLogService.BOUNCE), eq("END"), anyString());
  }

  @Test
  void newCorrelatedIdRunsAgain() {
    when(events.findIdsByStatus("CORRELATED"))
        .thenReturn(
            List.of("b0"),
            List.of("b0"),
            List.of("b0"),
            List.of("b0"),
            List.of("b0", "b1"),
            List.of("b0", "b1"),
            List.of("b0", "b1"),
            List.of("b0", "b1"));
    when(events.findByStatus("CORRELATED")).thenReturn(List.of());
    assertEquals(0, service.processDaily());
    assertEquals(0, service.processDaily());
    verify(events, times(2)).findByStatus("CORRELATED");
  }

  @Test
  void softLimitIndependent() {
    when(parameters.get(ParameterKeys.BOUNCE_HARD_LIMIT)).thenReturn(Optional.of("99"));
    when(parameters.get(ParameterKeys.BOUNCE_SOFT_LIMIT)).thenReturn(Optional.of("2"));
    List<SendQueueEntity> sends = new ArrayList<>();
    List<BounceEventEntity> bounces = new ArrayList<>();
    Instant t = Instant.parse("2026-01-01T00:00:00Z");
    for (int i = 0; i < 2; i++) {
      SendQueueEntity s = new SendQueueEntity("q" + i, "c", "m1", "a@x.com", "p", "SENT");
      s.setSentAt(t.plusSeconds(i));
      sends.add(s);
      BounceEventEntity b =
          new BounceEventEntity(
              "b" + i, "mb", t, null, "x", "f", "", new byte[0], "CORRELATED");
      b.setMemberId("m1");
      b.setSendQueueId("q" + i);
      b.setClassification("SOFT");
      bounces.add(b);
    }
    when(events.findIdsByStatus("CORRELATED"))
        .thenReturn(List.of("b0", "b1"), List.of("b0", "b1"), List.of("b0", "b1"), List.of());
    when(events.findByStatus("CORRELATED")).thenReturn(bounces);
    when(events.findByStatusAndMemberId("CORRELATED", "m1")).thenReturn(bounces);
    when(queue.findByMemberIdAndStatus("m1", "SENT")).thenReturn(sends);
    MemberEntity m = new MemberEntity("m1", "a@x.com", true, Instant.now());
    when(members.findById("m1")).thenReturn(Optional.of(m));
    when(members.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(events.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertEquals(1, service.processDaily());
    assertTrue(m.getDisableReason().contains("soft"));
  }
}
