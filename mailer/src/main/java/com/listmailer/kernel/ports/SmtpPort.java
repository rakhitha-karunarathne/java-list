package com.listmailer.kernel.ports;

import java.util.List;
import java.util.Map;

/** Outbound SMTP port; smtp-adapter implements against a Sending path. */
public interface SmtpPort {

  void send(SmtpMessage message);

  record Attachment(
      String filename, String contentType, byte[] content, String contentId, boolean inline) {
    public Attachment(String filename, String contentType, byte[] content) {
      this(filename, contentType, content, null, false);
    }
  }

  record SmtpMessage(
      String sendingPathId,
      String toEmail,
      String subject,
      String textBody,
      String htmlBody,
      List<Attachment> attachments,
      String listUnsubscribeUrl,
      Map<String, String> extraHeaders,
      /** Optional MAIL FROM override (VERP); null uses path Return-Path or From. */
      String envelopeFrom) {
    public SmtpMessage {
      attachments = attachments == null ? List.of() : List.copyOf(attachments);
      extraHeaders = extraHeaders == null ? Map.of() : Map.copyOf(extraHeaders);
    }

    public SmtpMessage(
        String sendingPathId,
        String toEmail,
        String subject,
        String textBody,
        String htmlBody,
        List<Attachment> attachments) {
      this(sendingPathId, toEmail, subject, textBody, htmlBody, attachments, null, Map.of(), null);
    }

    public SmtpMessage(
        String sendingPathId,
        String toEmail,
        String subject,
        String textBody,
        String htmlBody,
        List<Attachment> attachments,
        String listUnsubscribeUrl) {
      this(
          sendingPathId,
          toEmail,
          subject,
          textBody,
          htmlBody,
          attachments,
          listUnsubscribeUrl,
          Map.of(),
          null);
    }

    public SmtpMessage(
        String sendingPathId,
        String toEmail,
        String subject,
        String textBody,
        String htmlBody,
        List<Attachment> attachments,
        String listUnsubscribeUrl,
        Map<String, String> extraHeaders) {
      this(
          sendingPathId,
          toEmail,
          subject,
          textBody,
          htmlBody,
          attachments,
          listUnsubscribeUrl,
          extraHeaders,
          null);
    }
  }
}
