package com.listmailer.catalogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BounceMailboxesControllerTest {

  @Mock BounceMailboxRepository mailboxes;
  @Mock com.listmailer.mailbox.MailConnectionProbe probe;
  @Mock com.listmailer.bounce.BouncePollService poll;
  @Mock com.listmailer.mailbox.MailboxPollLock locks;
  @InjectMocks BounceMailboxesController controller;

  @Test
  void createDefaultsPollTo15() {
    when(mailboxes.save(any()))
        .thenAnswer(
            inv -> {
              BounceMailboxEntity e = inv.getArgument(0);
              return e;
            });
    var req =
        new BounceMailboxesController.MailboxRequest(
            "Inbox", "imap.example.com", 993, "u", "p", true, false, 0);
    var detail = controller.create(req);
    assertEquals(15, detail.pollIntervalMinutes());
    ArgumentCaptor<BounceMailboxEntity> captor = ArgumentCaptor.forClass(BounceMailboxEntity.class);
    org.mockito.Mockito.verify(mailboxes).save(captor.capture());
    assertEquals(15, captor.getValue().getPollIntervalMinutes());
  }
}
