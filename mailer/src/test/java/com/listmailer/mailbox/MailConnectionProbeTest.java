package com.listmailer.mailbox;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MailConnectionProbeTest {

  private final MailConnectionProbe probe = new MailConnectionProbe();

  @Test
  void smtpRejectsMissingFields() {
    MailConnectionProbe.Result r = probe.testSmtp("", 25, "u", "p", false, false);
    assertFalse(r.ok());
    assertTrue(r.message().contains("required"));
  }

  @Test
  void imapRejectsMissingFields() {
    MailConnectionProbe.Result r = probe.testImap("host", 0, "u", "p", false, false);
    assertFalse(r.ok());
  }

  @Test
  void smtpFailsFastAgainstClosedPort() {
    // 127.0.0.1:9 is discard; connection should fail with an error message
    MailConnectionProbe.Result r = probe.testSmtp("127.0.0.1", 9, "u", "p", false, false);
    assertFalse(r.ok());
    assertTrue(r.message() != null && !r.message().isBlank());
  }

  @Test
  void imapFailsFastAgainstClosedPort() {
    MailConnectionProbe.Result r = probe.testImap("127.0.0.1", 9, "u", "p", false, false);
    assertFalse(r.ok());
    assertTrue(r.message() != null && !r.message().isBlank());
  }
}
