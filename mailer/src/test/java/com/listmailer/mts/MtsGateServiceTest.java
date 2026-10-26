package com.listmailer.mts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.MailboxPort;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MtsGateServiceTest {

  @Mock MtsAuthorizedSenderRepository authorized;
  @Mock MtsDropService drops;
  @InjectMocks MtsGateService gates;

  @Test
  void dmarcFailRejectsEvenIfAuthorized() {
    when(drops.isBlocked("a@x.com")).thenReturn(false);
    when(authorized.existsByInboxIdAndEmail("i1", "a@x.com")).thenReturn(true);
    var raw =
        new MailboxPort.RawMessage(
            null,
            "s",
            "a@x.com",
            List.of(),
            Map.of("Authentication-Results", "mx; dmarc=fail"),
            new byte[0]);
    var d = gates.evaluate("i1", "Inbox", raw);
    assertEquals(MtsGateResult.DROP_SILENT, d.result());
    assertEquals("dmarc_fail", d.reason());
    assertFalse(d.countAutoBlock());
  }

  @Test
  void spamAbsentSkips() {
    when(drops.isBlocked("a@x.com")).thenReturn(false);
    when(authorized.existsByInboxIdAndEmail("i1", "a@x.com")).thenReturn(true);
    var raw =
        new MailboxPort.RawMessage(null, "s", "a@x.com", List.of(), Map.of(), new byte[0]);
    var d = gates.evaluate("i1", "Inbox", raw);
    assertEquals(MtsGateResult.ACCEPT, d.result());
  }

  @Test
  void strangerAuthPassCountsAutoBlock() {
    when(drops.isBlocked("stranger@x.com")).thenReturn(false);
    when(authorized.existsByInboxIdAndEmail("i1", "stranger@x.com")).thenReturn(false);
    when(authorized.existsByEmail("stranger@x.com")).thenReturn(false);
    var raw =
        new MailboxPort.RawMessage(
            null, "s", "stranger@x.com", List.of(), Map.of(), new byte[0]);
    var d = gates.evaluate("i1", "Inbox", raw);
    assertEquals(MtsGateResult.DROP_COUNTED, d.result());
    assertTrue(d.countAutoBlock());
  }

  @Test
  void crossInboxNotifyNoAutoBlock() {
    when(drops.isBlocked("a@x.com")).thenReturn(false);
    when(authorized.existsByInboxIdAndEmail("i1", "a@x.com")).thenReturn(false);
    when(authorized.existsByEmail("a@x.com")).thenReturn(true);
    var raw =
        new MailboxPort.RawMessage(null, "s", "a@x.com", List.of(), Map.of(), new byte[0]);
    var d = gates.evaluate("i1", "Inbox", raw);
    assertEquals(MtsGateResult.NOTIFY_CROSS_INBOX, d.result());
    assertFalse(d.countAutoBlock());
  }
}
