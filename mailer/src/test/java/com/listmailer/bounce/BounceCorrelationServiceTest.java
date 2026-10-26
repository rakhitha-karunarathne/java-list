package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BounceCorrelationServiceTest {

  @Mock BounceEventRepository events;
  @Mock SendQueueRepository queue;
  @Mock ParametersPort parameters;
  @Mock OpsHooksPort ops;
  @InjectMocks BounceCorrelationService service;

  @Test
  void tokenMatchCorrelatesWithoutDisable() {
    when(parameters.get(ParameterKeys.VERP_ENABLED)).thenReturn(Optional.of("false"));
    String tok = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";
    SendQueueEntity row = sent("q1", "m1", "a@x.com", tok);
    BounceEventEntity ev =
        event("e1", "Status: 5.1.1\nX-JavaList-Token: " + tok + "\n");
    when(events.findByStatus("INGESTED")).thenReturn(List.of(ev));
    when(queue.findByCorrelationToken(tok)).thenReturn(Optional.of(row));
    when(events.save(ev)).thenReturn(ev);

    service.correlatePending();

    assertEquals("CORRELATED", ev.getStatus());
    assertEquals("m1", ev.getMemberId());
    assertEquals("HARD", ev.getClassification());
    assertEquals("TOKEN", ev.getCorrelationSignal());
    verify(ops, never()).attentionAdmin(anyString(), anyString());
  }

  @Test
  void uncorrelatedAttentionNoMemberId() {
    when(parameters.get(ParameterKeys.VERP_ENABLED)).thenReturn(Optional.of("false"));
    BounceEventEntity ev = event("e2", "random noise without tokens");
    when(events.findByStatus("INGESTED")).thenReturn(List.of(ev));
    when(queue.findAll()).thenReturn(List.of());
    when(events.save(ev)).thenReturn(ev);

    service.correlatePending();

    assertEquals("UNCORRELATED", ev.getStatus());
    assertNull(ev.getMemberId());
    verify(ops).attentionAdmin(eq("Uncorrelated bounce"), contains("e2"));
    verify(ops).opsLog(eq("bounce.uncorrelated"), contains("e2"));
  }

  @Test
  void ambiguousSubjectEmailIsUncorrelated() {
    when(parameters.get(ParameterKeys.VERP_ENABLED)).thenReturn(Optional.of("false"));
    SendQueueEntity a = sent("q1", "m1", "same@x.com", "t1");
    a.setSubject("Hello");
    SendQueueEntity b = sent("q2", "m2", "same@x.com", "t2");
    b.setSubject("Hello");
    BounceEventEntity ev = event("e3", "Hello\nsame@x.com bounced");
    when(events.findByStatus("INGESTED")).thenReturn(List.of(ev));
    when(queue.findAll()).thenReturn(List.of(a, b));
    when(events.save(ev)).thenReturn(ev);

    service.correlatePending();

    assertEquals("UNCORRELATED", ev.getStatus());
  }

  private static SendQueueEntity sent(String id, String member, String email, String tok) {
    SendQueueEntity row = new SendQueueEntity(id, "c1", member, email, "p", "SENT");
    row.setCorrelationToken(tok);
    row.setSubject("Hello");
    return row;
  }

  private static BounceEventEntity event(String id, String body) {
    return new BounceEventEntity(
        id,
        "mb",
        Instant.now(),
        null,
        "Hello",
        "mta",
        "",
        body.getBytes(StandardCharsets.UTF_8),
        "INGESTED");
  }
}
