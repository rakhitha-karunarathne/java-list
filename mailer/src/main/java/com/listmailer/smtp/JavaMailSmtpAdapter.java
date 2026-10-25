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
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

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
      mime.setText(message.textBody(), "UTF-8");
      Transport.send(mime);
    } catch (Exception ex) {
      throw new IllegalStateException("SMTP send failed: " + ex.getMessage(), ex);
    }
  }
}
