package com.listmailer.noticemail;

import com.listmailer.users.UserEntity;
import com.listmailer.users.UserRepository;
import com.listmailer.users.UserRole;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Fan-out system notices to every enabled Admin via the Notice Sending Path. */
@Service
public class AdminNoticeMail {

  private static final Logger log = LoggerFactory.getLogger(AdminNoticeMail.class);

  private final NoticeMailService notice;
  private final UserRepository users;

  public AdminNoticeMail(NoticeMailService notice, UserRepository users) {
    this.notice = notice;
    this.users = users;
  }

  /** @return number of Admins for whom SMTP was invoked */
  public int send(String subject, String textBody, String htmlBody) {
    List<UserEntity> admins =
        users.findByRoleAndEnabledTrueAndReceiveNotificationEmailsTrue(UserRole.ADMIN);
    if (admins.isEmpty()) {
      log.warn(
          "Admin notice skipped (no enabled Admin with receive notification emails): subject={}",
          subject);
      return 0;
    }
    int n = 0;
    for (UserEntity admin : admins) {
      try {
        if (notice.send(admin.getEmail(), subject, textBody, htmlBody)) {
          n++;
        }
      } catch (RuntimeException ex) {
        log.warn(
            "Admin notice failed to={} subject={}: {}",
            admin.getEmail(),
            subject,
            ex.getMessage());
      }
    }
    return n;
  }
}
