package com.listmailer.users;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.secrets.SecretStore;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class MfaServiceSiteTitleTest {

  @Test
  void startSetupUsesConfiguredSiteTitleAsIssuer() {
    UserRepository users = mock(UserRepository.class);
    UserBackupCodeRepository backupCodes = mock(UserBackupCodeRepository.class);
    PasswordEncoder encoder = mock(PasswordEncoder.class);
    SecretStore secrets = mock(SecretStore.class);
    ParametersPort parameters = mock(ParametersPort.class);
    when(parameters.get(ParameterKeys.SITE_TITLE)).thenReturn(Optional.of("Acme Lists"));
    when(secrets.put(any(), anyString())).thenReturn("secret-id");
    when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));

    UserEntity user =
        new UserEntity("u1", "admin@example.com", "hash", UserRole.ADMIN, true, Instant.now());

    MfaService.Setup setup =
        new MfaService(users, backupCodes, encoder, secrets, parameters).startSetup(user);

    assertTrue(setup.otpAuthUrl().contains("Acme+Lists"));
    assertTrue(setup.otpAuthUrl().startsWith("otpauth://totp/"));
  }
}
