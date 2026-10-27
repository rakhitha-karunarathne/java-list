package com.listmailer.kernel.ports;

import java.util.List;
import java.util.Map;

/**
 * Shared IMAP mailbox-query utility (AD-12). Bounce and mail-to-send supply connection specs and
 * slice-specific consumers — no domain rules here.
 *
 * <p>Contract: {@link #poll} opens one inbox, streams unseen (or all unread) messages one at a time
 * into {@link MessageHandler}, then closes. Callers must not invoke overlapping polls for the same
 * poller type on one JVM. Handler failures abort the remaining pass and surface to the caller.
 */
public interface MailboxPort {

  void poll(MailboxConn conn, MessageHandler handler);

  /** Deletes messages already marked SEEN. Returns how many were removed. */
  int deleteSeen(MailboxConn conn);

  @FunctionalInterface
  interface MessageHandler {
    /**
     * {@link HandleResult#SEEN} marks read and keeps the message; {@link HandleResult#DELETE}
     * removes it; {@link HandleResult#LEAVE} leaves it unread for a later poll.
     */
    HandleResult onMessage(RawMessage message) throws Exception;
  }

  enum HandleResult {
    LEAVE,
    SEEN,
    DELETE
  }

  record MailboxConn(
      String host,
      int port,
      String username,
      String password,
      boolean useSsl,
      boolean useTls,
      String folder) {
    public MailboxConn {
      if (folder == null || folder.isBlank()) {
        folder = "INBOX";
      }
    }
  }

  record RawMessage(
      String messageId,
      String subject,
      String from,
      List<String> recipients,
      Map<String, String> headers,
      byte[] bytes) {}
}
