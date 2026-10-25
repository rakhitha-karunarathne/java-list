package com.listmailer.kernel.ports;

/** Outbound SMTP port; smtp-adapter implements later. */
public interface SmtpPort {

  void send(SmtpMessage message);

  record SmtpMessage(String from, String to, String subject, String textBody, String htmlBody) {}
}
