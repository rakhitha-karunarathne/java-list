package com.listmailer.noticemail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.users.UserEntity;
import com.listmailer.users.UserRepository;
import com.listmailer.users.UserRole;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminNoticeMailTest {

  @Mock NoticeMailService notice;
  @Mock UserRepository users;
  @InjectMocks AdminNoticeMail service;

  @Test
  void skipsWhenNoAdmins() {
    when(users.findByRoleAndEnabledTrue(UserRole.ADMIN)).thenReturn(List.of());
    assertEquals(0, service.send("s", "t", "h"));
    verify(notice, never()).send(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void sendsToEachEnabledAdmin() {
    UserEntity a =
        new UserEntity("1", "a@ex.com", "x", UserRole.ADMIN, true, Instant.now());
    UserEntity b =
        new UserEntity("2", "b@ex.com", "x", UserRole.ADMIN, true, Instant.now());
    when(users.findByRoleAndEnabledTrue(UserRole.ADMIN)).thenReturn(List.of(a, b));
    when(notice.send(eq("a@ex.com"), eq("s"), eq("t"), eq("h"))).thenReturn(true);
    when(notice.send(eq("b@ex.com"), eq("s"), eq("t"), eq("h"))).thenReturn(true);
    assertEquals(2, service.send("s", "t", "h"));
  }
}
