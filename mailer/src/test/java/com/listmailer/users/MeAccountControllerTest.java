package com.listmailer.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MeAccountControllerTest {

  @Mock UserRepository users;
  @Mock PasswordEncoder passwordEncoder;
  @Mock MfaService mfa;
  @Mock Authentication auth;

  MeAccountController controller;
  UserEntity user;

  @BeforeEach
  void setUp() {
    controller = new MeAccountController(users, passwordEncoder, mfa);
    user = new UserEntity("u1", "a@example.com", "hash", UserRole.LIST_SENDER, true, Instant.now());
    Jwt jwt =
        Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject("u1")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    when(auth.getPrincipal()).thenReturn(jwt);
    when(users.findById("u1")).thenReturn(Optional.of(user));
  }

  @Test
  void changesPasswordWhenCurrentMatches() {
    when(passwordEncoder.matches("old", "hash")).thenReturn(true);
    when(passwordEncoder.encode("new-secret")).thenReturn("newhash");

    var out =
        controller.changePassword(
            auth, new MeAccountController.PasswordChangeRequest("old", "new-secret", null));

    assertEquals(true, out.get("changed"));
    assertEquals("newhash", user.getPasswordHash());
    verify(users).save(user);
  }

  @Test
  void requiresMfaCodeWhenMfaEnabled() {
    user.setTotpEnabled(true);
    when(passwordEncoder.matches("old", "hash")).thenReturn(true);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.changePassword(
                    auth, new MeAccountController.PasswordChangeRequest("old", "new-secret", null)));
    assertEquals(401, ex.getStatusCode().value());
    assertEquals("MFA code required", ex.getReason());
    verify(users, never()).save(user);
  }

  @Test
  void rejectsInvalidMfaCodeWhenMfaEnabled() {
    user.setTotpEnabled(true);
    when(passwordEncoder.matches("old", "hash")).thenReturn(true);
    when(mfa.verifyCode(user, "000000")).thenReturn(false);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.changePassword(
                    auth,
                    new MeAccountController.PasswordChangeRequest("old", "new-secret", "000000")));
    assertEquals(401, ex.getStatusCode().value());
    assertEquals("Invalid MFA code", ex.getReason());
    verify(users, never()).save(user);
  }

  @Test
  void changesPasswordWithValidMfaWhenEnabled() {
    user.setTotpEnabled(true);
    when(passwordEncoder.matches("old", "hash")).thenReturn(true);
    when(mfa.verifyCode(user, "123456")).thenReturn(true);
    when(passwordEncoder.encode("new-secret")).thenReturn("newhash");

    var out =
        controller.changePassword(
            auth, new MeAccountController.PasswordChangeRequest("old", "new-secret", "123456"));

    assertEquals(true, out.get("changed"));
    verify(users).save(user);
  }

  @Test
  void meIncludesMfaEnabled() {
    user.setTotpEnabled(true);
    assertEquals(true, controller.me(auth).get("mfaEnabled"));
  }

  @Test
  void rejectsWrongCurrentPassword() {
    when(passwordEncoder.matches("nope", "hash")).thenReturn(false);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.changePassword(
                    auth, new MeAccountController.PasswordChangeRequest("nope", "new-secret", null)));
    assertEquals(401, ex.getStatusCode().value());
    verify(users, never()).save(user);
  }

  @Test
  void rejectsUnchangedOrBlankPassword() {
    when(passwordEncoder.matches("same", "hash")).thenReturn(true);
    ResponseStatusException same =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.changePassword(
                    auth, new MeAccountController.PasswordChangeRequest("same", "same", null)));
    assertEquals(400, same.getStatusCode().value());

    ResponseStatusException blank =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.changePassword(
                    auth, new MeAccountController.PasswordChangeRequest("same", "  ", null)));
    assertEquals(400, blank.getStatusCode().value());
    verify(users, never()).save(user);
  }

  @Test
  void returnsProfileWithDefaultTimeZone() {
    var out = controller.me(auth);
    assertEquals("a@example.com", out.get("email"));
    assertEquals("LIST_SENDER", out.get("role"));
    assertEquals("UTC", out.get("timeZone"));
    assertEquals(true, out.get("receiveNotificationEmails"));
    assertEquals(false, out.get("mfaEnabled"));
  }

  @Test
  void savesReceiveNotificationEmails() {
    var out =
        controller.setReceiveNotificationEmails(
            auth, new MeAccountController.NotificationEmailsRequest(false));
    assertEquals(false, out.get("receiveNotificationEmails"));
    assertEquals(false, user.isReceiveNotificationEmails());
    verify(users).save(user);
  }

  @Test
  void rejectsNullReceiveNotificationEmails() {
    ResponseStatusException nil =
        assertThrows(
            ResponseStatusException.class, () -> controller.setReceiveNotificationEmails(auth, null));
    assertEquals(400, nil.getStatusCode().value());
    verify(users, never()).save(user);
  }

  @Test
  void meReturnsStoredTimeZone() {
    user.setTimeZone("Asia/Colombo");
    assertEquals("Asia/Colombo", controller.me(auth).get("timeZone"));
  }

  @Test
  void meFallsBackWhenStoredZoneCorrupt() {
    user.setTimeZone("+05:30");
    assertEquals("UTC", controller.me(auth).get("timeZone"));
  }

  @Test
  void savesValidTimeZone() {
    var out =
        controller.setTimeZone(auth, new MeAccountController.TimeZoneRequest("Asia/Colombo"));
    assertEquals("Asia/Colombo", out.get("timeZone"));
    assertEquals("Asia/Colombo", user.getTimeZone());
    verify(users).save(user);
  }

  @Test
  void rejectsInvalidTimeZone() {
    ResponseStatusException bad =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.setTimeZone(auth, new MeAccountController.TimeZoneRequest("Not/AZone")));
    assertEquals(400, bad.getStatusCode().value());

    ResponseStatusException offset =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.setTimeZone(auth, new MeAccountController.TimeZoneRequest("+05:30")));
    assertEquals(400, offset.getStatusCode().value());
    verify(users, never()).save(user);
  }

  @Test
  void rejectsNullOrBlankTimeZone() {
    ResponseStatusException nil =
        assertThrows(ResponseStatusException.class, () -> controller.setTimeZone(auth, null));
    assertEquals(400, nil.getStatusCode().value());

    ResponseStatusException blank =
        assertThrows(
            ResponseStatusException.class,
            () -> controller.setTimeZone(auth, new MeAccountController.TimeZoneRequest("  ")));
    assertEquals(400, blank.getStatusCode().value());
    verify(users, never()).save(user);
  }
}
