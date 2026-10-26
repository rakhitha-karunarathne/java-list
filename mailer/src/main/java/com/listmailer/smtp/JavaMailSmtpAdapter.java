package com.listmailer.smtp;

import com.listmailer.catalogs.SendingPathEntity;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.ports.SmtpPort;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Primary
public class JavaMailSmtpAdapter implements SmtpPort {

  private final SendingPathRepository paths;

  public JavaMailSmtpAdapter(SendingPathRepository paths) {
    this.paths = paths;
  }

  @Override
  public void send(SmtpMessage message) {
    SendingPathEntity path =
        paths
            .findById(message.sendingPathId())
            .orElseThrow(() -> new IllegalArgumentException("unknown sending path"));
    Properties props = new Properties();
    props.put("mail.smtp.host", path.getHost());
    props.put("mail.smtp.port", String.valueOf(path.getPort()));
    props.put("mail.smtp.auth", "true");
    if (path.isUseTls()) {
      props.put("mail.smtp.starttls.enable", "true");
    }
    Session session =
        Session.getInstance(
            props,
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(path.getUsername(), path.getPassword());
              }
            });
    try {
      MimeMessage mime = new MimeMessage(session);
      mime.setFrom(new InternetAddress(path.getFromEmail(), path.getFromName()));
      mime.setRecipients(Message.RecipientType.TO, InternetAddress.parse(message.toEmail()));
      mime.setSubject(message.subject(), "UTF-8");
      if (StringUtils.hasText(message.listUnsubscribeUrl())) {
        String url = message.listUnsubscribeUrl();
        mime.setHeader("List-Unsubscribe", "<" + url + ">");
        mime.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
      }

      boolean hasHtml = StringUtils.hasText(message.htmlBody());
      boolean hasText = StringUtils.hasText(message.textBody());
      boolean hasAttachments = !message.attachments().isEmpty();

      MimeBodyPart bodyPart = new MimeBodyPart();
      if (hasHtml && hasText) {
        MimeMultipart alt = new MimeMultipart("alternative");
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(message.textBody(), "UTF-8");
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(message.htmlBody(), "text/html; charset=UTF-8");
        alt.addBodyPart(textPart);
        alt.addBodyPart(htmlPart);
        bodyPart.setContent(alt);
      } else if (hasHtml) {
        bodyPart.setContent(message.htmlBody(), "text/html; charset=UTF-8");
      } else {
        bodyPart.setText(hasText ? message.textBody() : "", "UTF-8");
      }

      if (hasAttachments) {
        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(bodyPart);
        for (Attachment a : message.attachments()) {
          MimeBodyPart filePart = new MimeBodyPart();
          filePart.setFileName(a.filename());
          filePart.setContent(a.content(), a.contentType());
          mixed.addBodyPart(filePart);
        }
        mime.setContent(mixed);
      } else if (hasHtml && hasText) {
        mime.setContent((MimeMultipart) bodyPart.getContent());
      } else if (hasHtml) {
        mime.setContent(message.htmlBody(), "text/html; charset=UTF-8");
      } else {
        mime.setText(hasText ? message.textBody() : "", "UTF-8");
      }
      Transport.send(mime);
    } catch (Exception ex) {
      throw new IllegalStateException("SMTP send failed: " + ex.getMessage(), ex);
    }
  }
}
