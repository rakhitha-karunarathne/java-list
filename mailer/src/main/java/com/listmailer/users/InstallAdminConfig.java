package com.listmailer.users;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.security.BootstrapAuthProperties;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class InstallAdminConfig {

  private static final Logger log = LoggerFactory.getLogger(InstallAdminConfig.class);

  @Bean
  ApplicationRunner installAdmin(
      UserRepository users, BootstrapAuthProperties bootstrap, PasswordEncoder passwordEncoder) {
    return args -> {
      if (users.count() > 0) {
        return;
      }
      if (!StringUtils.hasText(bootstrap.email()) || !StringUtils.hasText(bootstrap.password())) {
        throw new IllegalStateException(
            "mailer.bootstrap.email and mailer.bootstrap.password are required to seed the initial Admin");
      }
      String email = bootstrap.email().trim().toLowerCase();
      UserEntity admin =
          new UserEntity(
              Guids.newGuid(),
              email,
              passwordEncoder.encode(bootstrap.password()),
              UserRole.ADMIN,
              true,
              Instant.now());
      try {
        users.save(admin);
        log.info("Install seeded initial Admin user {}", email);
      } catch (DataIntegrityViolationException ex) {
        // shortcut: multi-instance race on empty DB, rare for V1 single-app Compose
        log.info("Initial Admin already present (concurrent install)");
      }
    };
  }
}
