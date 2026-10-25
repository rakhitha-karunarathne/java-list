package com.listmailer.users;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.security.BootstrapAuthProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class InstallAdminConfigTest {

  @Mock UserRepository users;

  @Test
  void seedsAdminWhenEmpty() throws Exception {
    when(users.count()).thenReturn(0L);
    PasswordEncoder encoder = new BCryptPasswordEncoder();
    var runner =
        new InstallAdminConfig()
            .installAdmin(users, new BootstrapAuthProperties("Admin@Example.com", "secret"), encoder);
    runner.run(new DefaultApplicationArguments());

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(users).save(captor.capture());
    UserEntity saved = captor.getValue();
    assertEquals("admin@example.com", saved.getEmail());
    assertEquals(UserRole.ADMIN, saved.getRole());
    assertTrue(saved.isEnabled());
    assertTrue(encoder.matches("secret", saved.getPasswordHash()));
  }

  @Test
  void skipsWhenUsersExist() throws Exception {
    when(users.count()).thenReturn(1L);
    var runner =
        new InstallAdminConfig()
            .installAdmin(
                users,
                new BootstrapAuthProperties("admin@example.com", "secret"),
                new BCryptPasswordEncoder());
    runner.run(new DefaultApplicationArguments());
    verify(users, never()).save(any());
  }

  @Test
  void rejectsBlankBootstrap() {
    when(users.count()).thenReturn(0L);
    var runner =
        new InstallAdminConfig()
            .installAdmin(
                users, new BootstrapAuthProperties(" ", "x"), new BCryptPasswordEncoder());
    assertThrows(
        IllegalStateException.class, () -> runner.run(new DefaultApplicationArguments()));
  }
}
