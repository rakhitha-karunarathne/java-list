package com.listmailer.mailbox;

import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Per-mailbox poll lock using DB flags. Stale locks older than the timeout are released. */
@Component
public class MailboxPollLock {

  private final Duration timeout;

  public MailboxPollLock(@Value("${mailer.poll.lock-timeout:1h}") Duration timeout) {
    this.timeout = timeout == null || timeout.isNegative() || timeout.isZero() ? Duration.ofHours(1) : timeout;
  }

  public Duration timeout() {
    return timeout;
  }

  public boolean stillHeld(boolean polling, Instant lastPollingUpdate, Instant now) {
    if (!polling) {
      return false;
    }
    if (lastPollingUpdate == null) {
      return false;
    }
    return lastPollingUpdate.plus(timeout).isAfter(now);
  }
}
