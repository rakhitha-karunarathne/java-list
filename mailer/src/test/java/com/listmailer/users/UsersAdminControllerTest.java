package com.listmailer.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.secrets.SecretStore;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UsersAdminControllerTest {

  @Mock UserRepository users;
  @Mock UserListGrantRepository grants;
  @Mock UserBackupCodeRepository backupCodes;
  @Mock SecretStore secrets;
  @Mock PasswordEncoder passwordEncoder;
  @InjectMocks UsersAdminController controller;

  @Test
  void refusesToDisableLastAdmin() {
    UserEntity admin =
        new UserEntity("a1", "admin@example.com", "hash", UserRole.ADMIN, true, Instant.now());
    when(users.findById("a1")).thenReturn(Optional.of(admin));
    when(users.findAll()).thenReturn(List.of(admin));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.patch(
                    "a1", new UsersAdminController.PatchUserRequest(false, null, null)));
    assertEquals(409, ex.getStatusCode().value());
  }

  @Test
  void patchesReceiveNotificationEmails() {
    UserEntity admin =
        new UserEntity("a1", "admin@example.com", "hash", UserRole.ADMIN, true, Instant.now());
    admin.setReceiveNotificationEmails(false);
    when(users.findById("a1")).thenReturn(Optional.of(admin));
    when(users.save(admin)).thenAnswer(inv -> inv.getArgument(0));

    var out =
        controller.patch(
            "a1", new UsersAdminController.PatchUserRequest(null, null, true));
    assertEquals(true, out.receiveNotificationEmails());
    assertEquals(true, admin.isReceiveNotificationEmails());
  }

  @Test
  void refusesToDeleteOwnAccount() {
    Authentication auth = authAs("a1");

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.delete("a1", auth));
    assertEquals(403, ex.getStatusCode().value());
    verify(users, never()).findById("a1");
    verify(grants, never()).deleteByUserId("a1");
  }

  @Test
  void refusesToDeleteLastAdmin() {
    UserEntity admin =
        new UserEntity("a1", "admin@example.com", "hash", UserRole.ADMIN, true, Instant.now());
    when(users.findById("a1")).thenReturn(Optional.of(admin));
    when(users.findAll()).thenReturn(List.of(admin));

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> controller.delete("a1", authAs("other")));
    assertEquals(409, ex.getStatusCode().value());
    verify(grants, never()).deleteByUserId("a1");
    verify(backupCodes, never()).deleteByUserId("a1");
    verify(secrets, never()).delete(org.mockito.ArgumentMatchers.any());
    verify(users, never()).delete(admin);
  }

  @Test
  void refusesToDeleteLastEnabledAdminWhenOtherAdminDisabled() {
    UserEntity enabled =
        new UserEntity("a1", "a1@example.com", "hash", UserRole.ADMIN, true, Instant.now());
    UserEntity disabled =
        new UserEntity("a2", "a2@example.com", "hash", UserRole.ADMIN, false, Instant.now());
    when(users.findById("a1")).thenReturn(Optional.of(enabled));
    when(users.findAll()).thenReturn(List.of(enabled, disabled));

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class, () -> controller.delete("a1", authAs("other")));
    assertEquals(409, ex.getStatusCode().value());
    verify(users, never()).delete(enabled);
  }

  @Test
  void deletesUserGrantsAndMfaData() {
    UserEntity sender =
        new UserEntity("s1", "sender@example.com", "hash", UserRole.LIST_SENDER, true, Instant.now());
    sender.setTotpSecretId("sec-1");
    when(users.findById("s1")).thenReturn(Optional.of(sender));

    controller.delete("s1", authAs("admin"));

    verify(grants).deleteByUserId("s1");
    verify(backupCodes).deleteByUserId("s1");
    verify(secrets).delete("sec-1");
    verify(users).delete(sender);
  }

  @Test
  void deletesAdminWhenAnotherEnabledAdminExists() {
    UserEntity a1 =
        new UserEntity("a1", "a1@example.com", "hash", UserRole.ADMIN, true, Instant.now());
    UserEntity a2 =
        new UserEntity("a2", "a2@example.com", "hash", UserRole.ADMIN, true, Instant.now());
    when(users.findById("a1")).thenReturn(Optional.of(a1));
    when(users.findAll()).thenReturn(List.of(a1, a2));

    controller.delete("a1", authAs("a2"));

    verify(users).delete(a1);
  }

  private static Authentication authAs(String userId) {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn(userId);
    Authentication auth = mock(Authentication.class);
    when(auth.getPrincipal()).thenReturn(jwt);
    return auth;
  }
}
