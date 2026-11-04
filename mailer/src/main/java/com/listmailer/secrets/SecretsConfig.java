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
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(SecretsProperties.class)
public class SecretsConfig {

  private static final Logger log = LoggerFactory.getLogger(SecretsConfig.class);

  /** Same default as historical application.yml / V41 when no env keys are set. */
  public static final String DEV_DEFAULT_KEY = "dev-only-secrets-key-32chars-minimum!";

  @Bean
  SecretsCrypto secretsCrypto(SecretsProperties props, Environment environment) {
    Map<Integer, String> byVersion = SecretsKeyEnv.read(environment);
    if (byVersion.isEmpty()) {
      byVersion = keysFromProperties(props);
    }
    if (byVersion.isEmpty()) {
      byVersion = new LinkedHashMap<>();
      byVersion.put(1, DEV_DEFAULT_KEY);
      log.warn(
          "No MAILER_SECRETS_KEY_* set; using built-in version 1 dev default. "
              + "Set MAILER_SECRETS_KEY_1 (and higher) in the environment for real deploys.");
    }
    return new SecretsCrypto(validateKeys(byVersion));
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
                + " secret(s) use a key_version above the highest configured key — restore that key"
                + " as MAILER_SECRETS_KEY_<version>");
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

  private static Map<Integer, String> keysFromProperties(SecretsProperties props) {
    Map<Integer, String> byVersion = new LinkedHashMap<>();
    for (SecretsProperties.KeyEntry e : props.getKeys()) {
      if (e == null) {
        throw new IllegalStateException("mailer.secrets.keys entry must not be null");
      }
      if (e.getVersion() < 1) {
        throw new IllegalStateException(
            "mailer.secrets.keys version must be >= 1 (got " + e.getVersion() + ")");
      }
      if (byVersion.put(e.getVersion(), e.getKey()) != null) {
        throw new IllegalStateException(
            "mailer.secrets.keys has duplicate version " + e.getVersion());
      }
    }
    return byVersion;
  }

  private static Map<Integer, String> validateKeys(Map<Integer, String> byVersion) {
    Map<Integer, String> out = new LinkedHashMap<>();
    for (Map.Entry<Integer, String> e : byVersion.entrySet()) {
      int version = e.getKey();
      String key = e.getValue();
      if (version < 1) {
        throw new IllegalStateException(
            "MAILER_SECRETS_KEY version must be >= 1 (got " + version + ")");
      }
      if (!StringUtils.hasText(key) || key.getBytes(StandardCharsets.UTF_8).length < 32) {
        throw new IllegalStateException(
            "MAILER_SECRETS_KEY_" + version + " must be at least 32 bytes");
      }
      out.put(version, key);
    }
    return out;
  }
}
