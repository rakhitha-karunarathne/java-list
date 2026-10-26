package com.listmailer.kernel.ports;

import java.util.List;

/** Outbound SMTP port; smtp-adapter implements against a Sending path. */
public interface SmtpPort {

  void send(SmtpMessage message);

  record Attachment(String filename, String contentType, byte[] content) {}

  record SmtpMessage(
      String sendingPathId,
      String toEmail,
      String subject,
      String textBody,
      String htmlBody,
      List<Attachment> attachments) {
    public SmtpMessage {
      attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
  }
}
