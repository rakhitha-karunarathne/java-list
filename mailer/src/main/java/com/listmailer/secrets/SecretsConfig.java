package com.listmailer.secrets;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(SecretsProperties.class)
public class SecretsConfig {

  private static final Logger log = LoggerFactory.getLogger(SecretsConfig.class);

  @Bean
  SecretsCrypto secretsCrypto(SecretsProperties props) {
    Map<Integer, String> byVersion = new LinkedHashMap<>();
    for (SecretsProperties.KeyEntry e : props.getKeys()) {
      if (e == null) {
        throw new IllegalStateException("mailer.secrets.keys entry must not be null");
      }
      if (e.getVersion() < 1) {
        throw new IllegalStateException(
            "mailer.secrets.keys version must be >= 1 (got " + e.getVersion() + ")");
      }
      if (!StringUtils.hasText(e.getKey()) || e.getKey().getBytes(StandardCharsets.UTF_8).length < 32) {
        throw new IllegalStateException(
            "mailer.secrets.keys version " + e.getVersion() + " key must be at least 32 bytes");
      }
      if (byVersion.put(e.getVersion(), e.getKey()) != null) {
        throw new IllegalStateException(
            "mailer.secrets.keys has duplicate version " + e.getVersion());
      }
    }
    return new SecretsCrypto(byVersion);
  }

  @Bean
  ApplicationRunner migrateSecretsOnStartup(SecretsProperties props, SecretStore store) {
    return args -> {
      if (!props.isMigrateOnStartup()) {
        return;
      }
      long newerThanConfig = store.countWithKeyVersionAboveCurrent();
      if (newerThanConfig > 0) {
        throw new IllegalStateException(
            "mailer.secrets.migrate-on-startup: "
                + newerThanConfig
                + " secret(s) use a key_version above the highest configured key — restore that key in mailer.secrets.keys");
      }
      SecretStore.RewrapResult r = store.rewrapAllToLatest();
      log.info(
          "Secrets migrate-on-startup: migrated={} alreadyCurrent={} failed={} keyVersion={}",
          r.migrated(),
          r.alreadyCurrent(),
          r.failed(),
          r.currentKeyVersion());
      if (r.failed() > 0) {
        throw new IllegalStateException(
            "mailer.secrets.migrate-on-startup failed for " + r.failed() + " secret(s)");
      }
    };
  }
}
