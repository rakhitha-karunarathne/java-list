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
  @Mock Authentication auth;

  MeAccountController controller;
  UserEntity user;

  @BeforeEach
  void setUp() {
    controller = new MeAccountController(users, passwordEncoder);
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
            auth, new MeAccountController.PasswordChangeRequest("old", "new-secret"));

    assertEquals(true, out.get("changed"));
    assertEquals("newhash", user.getPasswordHash());
    verify(users).save(user);
  }

  @Test
  void rejectsWrongCurrentPassword() {
    when(passwordEncoder.matches("nope", "hash")).thenReturn(false);

    ResponseStatusException ex =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.changePassword(
                    auth, new MeAccountController.PasswordChangeRequest("nope", "new-secret")));
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
                    auth, new MeAccountController.PasswordChangeRequest("same", "same")));
    assertEquals(400, same.getStatusCode().value());

    ResponseStatusException blank =
        assertThrows(
            ResponseStatusException.class,
            () ->
                controller.changePassword(
                    auth, new MeAccountController.PasswordChangeRequest("same", "  ")));
    assertEquals(400, blank.getStatusCode().value());
    verify(users, never()).save(user);
  }
}
