package com.listmailer.noticemail;

import com.listmailer.catalogs.NoticeSendingPathRepository;
import com.listmailer.kernel.ports.SmtpPort;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** System/consent mail via Notice sending path only (never Campaign queue). */
@Service
public class NoticeMailService {

  private static final Logger log = LoggerFactory.getLogger(NoticeMailService.class);

  private final NoticeSendingPathRepository notice;
  private final SmtpPort smtp;

  public NoticeMailService(NoticeSendingPathRepository notice, SmtpPort smtp) {
    this.notice = notice;
    this.smtp = smtp;
  }

  /**
   * Sends even when Member is bounce-disabled (R3 bypass). No-op when Notice sending path is unset
   * — never throws for missing path.
   *
   * @return true if SMTP was invoked
   */
  public boolean send(String toEmail, String subject, String textBody, String htmlBody) {
    String pathId =
        notice
            .findById((byte) 1)
            .map(r -> r.getSendingPathId())
            .filter(StringUtils::hasText)
            .orElse(null);
    if (pathId == null) {
      log.warn("Notice mail skipped (path not configured): to={}", toEmail);
      return false;
    }
    smtp.send(
        new SmtpPort.SmtpMessage(
            pathId, toEmail, subject, textBody, htmlBody == null ? "" : htmlBody, List.of(), null));
    return true;
  }
}
