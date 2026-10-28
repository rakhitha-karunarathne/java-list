package com.listmailer.catalogs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.mailbox.MailConnectionProbe;
import com.listmailer.secrets.SecretStore;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BounceMailboxConnectionTest {

  @Mock BounceMailboxRepository mailboxes;
  @Mock MailConnectionProbe probe;
  @Mock com.listmailer.bounce.BouncePollService poll;
  @Mock com.listmailer.mailbox.MailboxPollLock locks;
  @Mock SecretStore secrets;
  @InjectMocks BounceMailboxesController controller;

  @Test
  void testConnectionUsesStoredPasswordWhenBlank() {
    when(mailboxes.findById("b1"))
        .thenReturn(
            Optional.of(
                new BounceMailboxEntity(
                    "b1", "B", "h", 143, "u", "sid-1", false, 15, Instant.now())));
    when(secrets.requirePlain("sid-1")).thenReturn("secret");
    when(probe.testImap(anyString(), anyInt(), anyString(), anyString(), anyBoolean(), anyBoolean()))
        .thenReturn(MailConnectionProbe.Result.success());

    var r =
        controller.testConnection(
            new BounceMailboxesController.ConnectionTestRequest(
                "h", 143, "u", "", false, false, "b1"));

    assertTrue(r.ok());
    verify(probe).testImap(eq("h"), eq(143), eq("u"), eq("secret"), eq(false), eq(false));
  }

  @Test
  void testConnectionReturnsFailureMessage() {
    when(probe.testImap(anyString(), anyInt(), anyString(), anyString(), anyBoolean(), anyBoolean()))
        .thenReturn(MailConnectionProbe.Result.failure("refused"));

    var r =
        controller.testConnection(
            new BounceMailboxesController.ConnectionTestRequest(
                "h", 143, "u", "p", true, false, null));

    assertFalse(r.ok());
    org.junit.jupiter.api.Assertions.assertEquals("refused", r.message());
  }
}
