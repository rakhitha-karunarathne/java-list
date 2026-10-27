package com.listmailer.mailbox;

import com.listmailer.kernel.ports.MailboxPort;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Jakarta Mail IMAP implementation of mailbox-query. One connection per poll call. */
@Component
@Primary
public class ImapMailboxQuery implements MailboxPort {

  @Override
  public void poll(MailboxConn conn, MessageHandler handler) {
    Properties props =
        MailSecurity.imap(conn.host(), conn.port(), conn.useSsl(), conn.useTls());
    String protocol = MailSecurity.imapProtocol(conn.useSsl());
    Session session = Session.getInstance(props);
    try (Store store = session.getStore(protocol)) {
      store.connect(conn.host(), conn.port(), conn.username(), conn.password());
      Folder folder = store.getFolder(conn.folder());
      folder.open(Folder.READ_WRITE);
      boolean expunge = false;
      try {
        Message[] messages = folder.getMessages();
        for (Message message : messages) {
          if (message.isSet(Flags.Flag.SEEN) || message.isSet(Flags.Flag.DELETED)) {
            continue;
          }
          RawMessage raw = toRaw(message);
          HandleResult result = handler.onMessage(raw);
          if (result == HandleResult.DELETE) {
            message.setFlag(Flags.Flag.DELETED, true);
            expunge = true;
          } else if (result == HandleResult.SEEN) {
            message.setFlag(Flags.Flag.SEEN, true);
          }
        }
      } finally {
        if (folder.isOpen()) {
          folder.close(expunge);
        }
      }
    } catch (Exception ex) {
      throw new MailboxQueryException(
          "IMAP poll failed for " + conn.host() + ":" + conn.port() + " — " + ex.getMessage(), ex);
    }
  }

  @Override
  public int deleteSeen(MailboxConn conn) {
    Properties props =
        MailSecurity.imap(conn.host(), conn.port(), conn.useSsl(), conn.useTls());
    String protocol = MailSecurity.imapProtocol(conn.useSsl());
    Session session = Session.getInstance(props);
    try (Store store = session.getStore(protocol)) {
      store.connect(conn.host(), conn.port(), conn.username(), conn.password());
      Folder folder = store.getFolder(conn.folder());
      folder.open(Folder.READ_WRITE);
      int deleted = 0;
      try {
        Message[] messages = folder.getMessages();
        for (Message message : messages) {
          if (message.isSet(Flags.Flag.DELETED)) {
            continue;
          }
          if (message.isSet(Flags.Flag.SEEN)) {
            message.setFlag(Flags.Flag.DELETED, true);
            deleted++;
          }
        }
      } finally {
        if (folder.isOpen()) {
          folder.close(deleted > 0);
        }
      }
      return deleted;
    } catch (Exception ex) {
      throw new MailboxQueryException(
          "IMAP delete-seen failed for "
              + conn.host()
              + ":"
              + conn.port()
              + " — "
              + ex.getMessage(),
          ex);
    }
  }

  private static RawMessage toRaw(Message message) throws Exception {
    String messageId = null;
    String[] ids = message.getHeader("Message-ID");
    if (ids != null && ids.length > 0) {
      messageId = ids[0];
    }
    String subject = message.getSubject() == null ? "" : message.getSubject();
    String from = "";
    if (message.getFrom() != null && message.getFrom().length > 0) {
      from = InternetAddress.toString(message.getFrom());
    }
    List<String> recipients = new ArrayList<>();
    if (message.getAllRecipients() != null) {
      for (var a : message.getAllRecipients()) {
        recipients.add(a.toString());
      }
    }
    Map<String, String> headers = new LinkedHashMap<>();
    Enumeration<?> all = ((MimeMessage) message).getAllHeaders();
    while (all.hasMoreElements()) {
      jakarta.mail.Header h = (jakarta.mail.Header) all.nextElement();
      headers.putIfAbsent(h.getName(), h.getValue());
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    message.writeTo(bos);
    return new RawMessage(messageId, subject, from, List.copyOf(recipients), Map.copyOf(headers), bos.toByteArray());
  }
}
