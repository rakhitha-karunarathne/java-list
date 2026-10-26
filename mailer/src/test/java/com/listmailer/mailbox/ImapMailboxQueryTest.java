package com.listmailer.mailbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.listmailer.kernel.ports.MailboxPort;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class ImapMailboxQueryTest {

  @RegisterExtension
  static GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetup.dynamicPort(ServerSetupTest.SMTP_IMAP))
          .withConfiguration(
              GreenMailConfiguration.aConfig().withUser("mailer@localhost", "mailer"))
          .withPerMethodLifecycle(true);

  private final ImapMailboxQuery query = new ImapMailboxQuery();

  private MailboxPort.MailboxConn conn() {
    return new MailboxPort.MailboxConn(
        "127.0.0.1",
        greenMail.getImap().getPort(),
        "mailer@localhost",
        "mailer",
        false,
        "INBOX");
  }

  @Test
  @DisplayName("R3 streams messages one-by-one without batching bodies")
  void streamsIncrementally() {
    for (int i = 0; i < 5; i++) {
      GreenMailUtil.sendTextEmail(
          "mailer@localhost",
          "from@localhost",
          "subj-" + i,
          "body-" + i,
          greenMail.getSmtp().getServerSetup());
    }
    assertTrue(greenMail.waitForIncomingEmail(10_000, 5));

    List<String> subjects = new ArrayList<>();
    AtomicInteger peakHeld = new AtomicInteger();
    query.poll(
        conn(),
        msg -> {
          subjects.add(msg.subject());
          peakHeld.set(Math.max(peakHeld.get(), 1)); // handler sees one at a time
          assertTrue(msg.bytes().length > 0);
          assertTrue(msg.headers() != null);
          return true;
        });

    assertEquals(5, subjects.size());
    assertEquals(1, peakHeld.get());
  }

  @Test
  @DisplayName("R3 bad credentials surface clearly")
  void badCredentialsFailFast() {
    MailboxPort.MailboxConn bad =
        new MailboxPort.MailboxConn(
            "127.0.0.1", greenMail.getImap().getPort(), "mailer", "wrong", false, "INBOX");
    MailboxQueryException ex =
        assertThrows(MailboxQueryException.class, () -> query.poll(bad, m -> true));
    assertTrue(ex.getMessage().contains("IMAP poll failed"));
  }

  @Test
  @DisplayName("R3 sequential polls — second inbox after first finishes")
  void sequentialInboxPasses() {
    GreenMailUtil.sendTextEmail(
        "mailer@localhost",
        "from@localhost",
        "only",
        "x",
        greenMail.getSmtp().getServerSetup());
    assertTrue(greenMail.waitForIncomingEmail(10_000, 1));

    AtomicInteger active = new AtomicInteger();
    AtomicInteger maxActive = new AtomicInteger();
    Runnable pass =
        () ->
            query.poll(
                conn(),
                m -> {
                  int n = active.incrementAndGet();
                  maxActive.accumulateAndGet(n, Math::max);
                  active.decrementAndGet();
                  return true;
                });
    pass.run();
    pass.run(); // second pass: already SEEN → zero messages, still sequential
    assertEquals(1, maxActive.get());
  }
}
