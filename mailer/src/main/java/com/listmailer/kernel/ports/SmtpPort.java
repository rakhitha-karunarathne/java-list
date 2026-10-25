package com.listmailer.kernel.ports;

/** Outbound SMTP port; smtp-adapter implements against a Sending path. */
public interface SmtpPort {

  void send(SmtpMessage message);

  record SmtpMessage(
      String sendingPathId, String toEmail, String subject, String textBody, String htmlBody) {}
}
