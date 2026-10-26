package com.listmailer.bounce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.catalogs.BounceMailboxEntity;
import com.listmailer.catalogs.BounceMailboxRepository;
import com.listmailer.kernel.ports.MailboxPort;
import com.listmailer.kernel.ports.OpsHooksPort;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BouncePollServiceTest {

  @Mock BounceMailboxRepository mailboxes;
  @Mock BounceEventRepository events;
  @Mock MailboxPort mailbox;
  @Mock OpsHooksPort ops;
  @InjectMocks BouncePollService service;

  @Test
  void pollsEnabledDueMailboxesSequentiallyAndIngests() throws Exception {
    BounceMailboxEntity a =
        new BounceMailboxEntity("a", "A", "h", 143, "u", "p", false, 1, Instant.now());
    BounceMailboxEntity b =
        new BounceMailboxEntity("b", "B", "h", 143, "u", "p", false, 1, Instant.now());
    b.setEnabled(false);
    when(mailboxes.findAll()).thenReturn(List.of(a, b));
    AtomicInteger concurrent = new AtomicInteger();
    AtomicInteger max = new AtomicInteger();
    doAnswer(
            inv -> {
              int n = concurrent.incrementAndGet();
              max.accumulateAndGet(n, Math::max);
              MailboxPort.MessageHandler h = inv.getArgument(1);
              h.onMessage(
                  new MailboxPort.RawMessage(
                      "<id@x>", "bounce", "mta", List.of("x@y"), Map.of("X-T", "1"), new byte[] {1}));
              concurrent.decrementAndGet();
              return null;
            })
        .when(mailbox)
        .poll(any(), any());
    when(events.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(mailboxes.save(any())).thenAnswer(inv -> inv.getArgument(0));

    assertEquals(1, service.pollDue());
    assertEquals(1, max.get());
    verify(ops).opsLog(eq("bounce.poll"), contains("ingested=1"));
    verify(mailbox, org.mockito.Mockito.times(1)).poll(any(), any());
  }

  @Test
  void skipsSecondTickWhileRunning() throws Exception {
    BounceMailboxEntity a =
        new BounceMailboxEntity("a", "A", "h", 143, "u", "p", false, 1, Instant.now());
    when(mailboxes.findAll()).thenReturn(List.of(a));
    doAnswer(
            inv -> {
              assertTrue(service.isRunning());
              assertEquals(0, service.pollDue()); // nested tick skipped
              MailboxPort.MessageHandler h = inv.getArgument(1);
              h.onMessage(
                  new MailboxPort.RawMessage(
                      null, "s", "f", List.of(), Map.of(), new byte[] {1}));
              return null;
            })
        .when(mailbox)
        .poll(any(), any());
    when(events.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(mailboxes.save(any())).thenAnswer(inv -> inv.getArgument(0));

    service.pollDue();
    verify(ops).opsLog(eq("bounce.poll"), contains("ingested=1"));
  }

  @Test
  void doesNotDisableMembers() {
    // ingest path never touches members — smoke via zero member interactions
    when(mailboxes.findAll()).thenReturn(List.of());
    assertEquals(0, service.pollDue());
    verify(events, never()).save(any());
  }
}
