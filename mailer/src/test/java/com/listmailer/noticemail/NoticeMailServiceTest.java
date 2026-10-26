package com.listmailer.noticemail;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.catalogs.NoticeSendingPathEntity;
import com.listmailer.catalogs.NoticeSendingPathRepository;
import com.listmailer.kernel.ports.SmtpPort;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoticeMailServiceTest {

  @Mock NoticeSendingPathRepository notice;
  @Mock SmtpPort smtp;
  @InjectMocks NoticeMailService service;

  @Test
  void skipsWhenPathNotConfigured() {
    when(notice.findById((byte) 1)).thenReturn(Optional.empty());

    assertFalse(service.send("a@b.com", "s", "t", "<p>h</p>"));
    verify(smtp, never()).send(any());
  }

  @Test
  void skipsWhenPathIdBlank() {
    when(notice.findById((byte) 1)).thenReturn(Optional.of(new NoticeSendingPathEntity(null)));

    assertFalse(service.send("a@b.com", "s", "t", null));
    verify(smtp, never()).send(any());
  }

  @Test
  void sendsWhenPathSet() {
    when(notice.findById((byte) 1)).thenReturn(Optional.of(new NoticeSendingPathEntity("path-1")));

    assertTrue(service.send("a@b.com", "s", "t", "<p>h</p>"));
    verify(smtp).send(any());
  }
}
