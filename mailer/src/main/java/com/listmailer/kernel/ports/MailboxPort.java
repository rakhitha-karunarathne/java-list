package com.listmailer.kernel.ports;

/** IMAP mailbox access; mailbox-query implements later. */
public interface MailboxPort {

  void poll(String mailboxId, MessageHandler handler);

  @FunctionalInterface
  interface MessageHandler {
    void onMessage(RawMessage message);
  }

  record RawMessage(String messageId, byte[] bytes) {}
}
