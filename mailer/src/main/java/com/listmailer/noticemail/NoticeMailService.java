package com.listmailer.noticemail;

import com.listmailer.catalogs.NoticeSendingPathRepository;
import com.listmailer.kernel.ports.SmtpPort;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/** System/consent mail via Notice sending path only (never Campaign queue). */
@Service
public class NoticeMailService {

  private final NoticeSendingPathRepository notice;
  private final SmtpPort smtp;

  public NoticeMailService(NoticeSendingPathRepository notice, SmtpPort smtp) {
    this.notice = notice;
    this.smtp = smtp;
  }

  /** Sends even when Member is bounce-disabled (R3 bypass). */
  public void send(String toEmail, String subject, String textBody, String htmlBody) {
    String pathId =
        notice
            .findById((byte) 1)
            .map(r -> r.getSendingPathId())
            .filter(StringUtils::hasText)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Notice sending path not configured"));
    smtp.send(
        new SmtpPort.SmtpMessage(
            pathId, toEmail, subject, textBody, htmlBody == null ? "" : htmlBody, List.of(), null));
  }
}
