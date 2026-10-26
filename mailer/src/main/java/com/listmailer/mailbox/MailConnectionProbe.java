package com.listmailer.mailbox;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import java.util.Properties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/** Connect-only probes for operator Test Connection (no send, no poll). */
@Component
public class MailConnectionProbe {

  private static final String TIMEOUT_MS = "10000";

  public Result testSmtp(String host, int port, String username, String password, boolean useTls) {
    if (!StringUtils.hasText(host) || port <= 0 || !StringUtils.hasText(username) || !StringUtils.hasText(password)) {
      return Result.failure("host, port, username, and password are required");
    }
    Properties props = new Properties();
    props.put("mail.smtp.host", host.trim());
    props.put("mail.smtp.port", String.valueOf(port));
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.connectiontimeout", TIMEOUT_MS);
    props.put("mail.smtp.timeout", TIMEOUT_MS);
    props.put("mail.smtp.writetimeout", TIMEOUT_MS);
    if (useTls) {
      props.put("mail.smtp.starttls.enable", "true");
    }
    Session session =
        Session.getInstance(
            props,
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
              }
            });
    try {
      Transport transport = session.getTransport("smtp");
      try {
        transport.connect(host.trim(), port, username, password);
      } finally {
        transport.close();
      }
      return Result.success();
    } catch (Exception ex) {
      return Result.failure(rootMessage(ex));
    }
  }

  public Result testImap(String host, int port, String username, String password, boolean useSsl) {
    if (!StringUtils.hasText(host) || port <= 0 || !StringUtils.hasText(username) || !StringUtils.hasText(password)) {
      return Result.failure("host, port, username, and password are required");
    }
    String protocol = useSsl ? "imaps" : "imap";
    Properties props = new Properties();
    props.put("mail.store.protocol", protocol);
    props.put("mail." + protocol + ".host", host.trim());
    props.put("mail." + protocol + ".port", String.valueOf(port));
    props.put("mail." + protocol + ".connectiontimeout", TIMEOUT_MS);
    props.put("mail." + protocol + ".timeout", TIMEOUT_MS);
    if (useSsl) {
      props.put("mail.imaps.ssl.enable", "true");
      props.put("mail.imaps.ssl.trust", "*");
    }
    Session session = Session.getInstance(props);
    try (Store store = session.getStore(protocol)) {
      store.connect(host.trim(), port, username, password);
      return Result.success();
    } catch (Exception ex) {
      return Result.failure(rootMessage(ex));
    }
  }

  static String rootMessage(Throwable ex) {
    Throwable cur = ex;
    String last = ex.getMessage();
    while (cur != null) {
      if (StringUtils.hasText(cur.getMessage())) {
        last = cur.getMessage();
      }
      cur = cur.getCause();
    }
    if (!StringUtils.hasText(last)) {
      return ex.getClass().getSimpleName();
    }
    // Cap length so UI/API clients are not flooded by nested mail stack traces.
    return last.length() > 500 ? last.substring(0, 500) + "…" : last;
  }

  public record Result(boolean ok, String message) {
    public static Result success() {
      return new Result(true, null);
    }

    public static Result failure(String message) {
      return new Result(false, message);
    }
  }
}
