package com.listmailer.smtp;

import com.listmailer.catalogs.SendingPathEntity;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.mailbox.MailSecurity;
import com.listmailer.secrets.SecretStore;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Properties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Primary
public class JavaMailSmtpAdapter implements SmtpPort {

  private final SendingPathRepository paths;
  private final SecretStore secrets;

  public JavaMailSmtpAdapter(SendingPathRepository paths, SecretStore secrets) {
    this.paths = paths;
    this.secrets = secrets;
  }

  @Override
  public void send(SmtpMessage message) {
    SendingPathEntity path =
        paths
            .findById(message.sendingPathId())
            .orElseThrow(() -> new IllegalArgumentException("unknown sending path"));
    Properties props =
        MailSecurity.smtp(path.getHost(), path.getPort(), path.isUseSsl(), path.isUseTls());
    String envelopeFrom =
        StringUtils.hasText(path.getReturnPathEmail())
            ? path.getReturnPathEmail().trim()
            : path.getFromEmail();
    props.put("mail.smtp.from", envelopeFrom);
    Session session =
        Session.getInstance(
            props,
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    path.getUsername(), secrets.requirePlain(path.getPasswordSecretId()));
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
      if (message.extraHeaders() != null) {
        for (var e : message.extraHeaders().entrySet()) {
          if (StringUtils.hasText(e.getKey()) && e.getValue() != null) {
            mime.setHeader(e.getKey(), e.getValue());
          }
        }
      }

      List<Attachment> inline =
          message.attachments().stream().filter(Attachment::inline).toList();
      List<Attachment> files =
          message.attachments().stream().filter(a -> !a.inline()).toList();

      boolean hasHtml = StringUtils.hasText(message.htmlBody());
      boolean hasText = StringUtils.hasText(message.textBody());

      MimeBodyPart root = buildBody(message, hasHtml, hasText, inline);
      if (!files.isEmpty()) {
        MimeMultipart mixed = new MimeMultipart("mixed");
        mixed.addBodyPart(root);
        for (Attachment a : files) {
          MimeBodyPart filePart = new MimeBodyPart();
          filePart.setFileName(a.filename());
          filePart.setContent(a.content(), a.contentType());
          mixed.addBodyPart(filePart);
        }
        mime.setContent(mixed);
      } else if (root.getContent() instanceof MimeMultipart mp) {
        mime.setContent(mp);
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

  private static MimeBodyPart buildBody(
      SmtpMessage message, boolean hasHtml, boolean hasText, List<Attachment> inline)
      throws Exception {
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

    if (inline.isEmpty()) {
      return bodyPart;
    }
    MimeMultipart related = new MimeMultipart("related");
    related.addBodyPart(bodyPart);
    for (Attachment a : inline) {
      MimeBodyPart img = new MimeBodyPart();
      img.setContent(a.content(), a.contentType());
      img.setDisposition(MimeBodyPart.INLINE);
      String cid = a.contentId();
      if (StringUtils.hasText(cid)) {
        if (!cid.startsWith("<")) {
          cid = "<" + cid + ">";
        }
        img.setContentID(cid);
      }
      if (StringUtils.hasText(a.filename())) {
        img.setFileName(a.filename());
      }
      related.addBodyPart(img);
    }
    MimeBodyPart wrap = new MimeBodyPart();
    wrap.setContent(related);
    return wrap;
  }
}
