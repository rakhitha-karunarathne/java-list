package com.listmailer.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UsersAdminControllerTest {

  @Mock UserRepository users;
  @Mock UserListGrantRepository grants;
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
            () -> controller.patch("a1", new UsersAdminController.PatchUserRequest(false, null)));
    assertEquals(409, ex.getStatusCode().value());
  }
}
