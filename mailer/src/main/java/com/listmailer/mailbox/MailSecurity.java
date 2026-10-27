package com.listmailer.mailbox;

import java.util.Properties;

/** SSL is implicit (smtps/imaps). TLS is STARTTLS on the plain protocol. */
public final class MailSecurity {

  private MailSecurity() {}

  public static void check(boolean useSsl, boolean useTls) {
    if (useSsl && useTls) {
      throw new IllegalArgumentException("choose SSL or TLS, not both");
    }
  }

  public static String smtpProtocol(boolean useSsl) {
    return useSsl ? "smtps" : "smtp";
  }

  public static Properties smtp(String host, int port, boolean useSsl, boolean useTls) {
    check(useSsl, useTls);
    String protocol = smtpProtocol(useSsl);
    Properties props = new Properties();
    props.put("mail.transport.protocol", protocol);
    props.put("mail." + protocol + ".host", host);
    props.put("mail." + protocol + ".port", String.valueOf(port));
    props.put("mail." + protocol + ".auth", "true");
    if (useSsl) {
      props.put("mail.smtps.ssl.enable", "true");
      // shortcut: trust any cert, same as IMAP SSL
      props.put("mail.smtps.ssl.trust", "*");
    } else if (useTls) {
      props.put("mail.smtp.starttls.enable", "true");
    }
    return props;
  }

  public static String imapProtocol(boolean useSsl) {
    return useSsl ? "imaps" : "imap";
  }

  public static Properties imap(String host, int port, boolean useSsl, boolean useTls) {
    check(useSsl, useTls);
    String protocol = imapProtocol(useSsl);
    Properties props = new Properties();
    props.put("mail.store.protocol", protocol);
    props.put("mail." + protocol + ".host", host);
    props.put("mail." + protocol + ".port", String.valueOf(port));
    if (useSsl) {
      props.put("mail.imaps.ssl.enable", "true");
      props.put("mail.imaps.ssl.trust", "*");
    } else if (useTls) {
      props.put("mail.imap.starttls.enable", "true");
      props.put("mail.imap.ssl.trust", "*");
    }
    return props;
  }
}
