package com.listmailer.mailbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;
import org.junit.jupiter.api.Test;

class MailSecurityTest {

  @Test
  void smtpSslIsImplicit() {
    Properties props = MailSecurity.smtp("mail.example", 465, true, false);
    assertEquals("smtps", props.getProperty("mail.transport.protocol"));
    assertEquals("true", props.getProperty("mail.smtps.ssl.enable"));
    assertNull(props.getProperty("mail.smtp.starttls.enable"));
  }

  @Test
  void smtpTlsIsStarttls() {
    Properties props = MailSecurity.smtp("mail.example", 587, false, true);
    assertEquals("smtp", props.getProperty("mail.transport.protocol"));
    assertEquals("true", props.getProperty("mail.smtp.starttls.enable"));
    assertNull(props.getProperty("mail.smtps.ssl.enable"));
  }

  @Test
  void imapTlsIsStarttls() {
    Properties props = MailSecurity.imap("mail.example", 143, false, true);
    assertEquals("imap", props.getProperty("mail.store.protocol"));
    assertEquals("true", props.getProperty("mail.imap.starttls.enable"));
  }

  @Test
  void imapSslStaysImaps() {
    Properties props = MailSecurity.imap("mail.example", 993, true, false);
    assertEquals("imaps", props.getProperty("mail.store.protocol"));
    assertEquals("true", props.getProperty("mail.imaps.ssl.enable"));
  }

  @Test
  void refusesBoth() {
    assertThrows(IllegalArgumentException.class, () -> MailSecurity.smtp("h", 465, true, true));
    assertThrows(IllegalArgumentException.class, () -> MailSecurity.imap("h", 993, true, true));
  }
}
