package com.listmailer.mailbox;

/** Connection/auth/IO failure from mailbox-query — callers map to Ops/Attention. */
public class MailboxQueryException extends RuntimeException {

  public MailboxQueryException(String message, Throwable cause) {
    super(message, cause);
  }
}
