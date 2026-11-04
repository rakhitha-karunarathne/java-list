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
    Integer active = resolveActiveKeyVersion(props, environment);
    return new SecretsCrypto(validateKeys(byVersion), active);
  }

  @Bean
  ApplicationRunner migrateSecretsOnStartup(SecretsProperties props, SecretStore store) {
    return args -> {
      if (!props.isMigrateOnStartup()) {
        return;
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

  /**
   * Explicit active version from env (preferred) or YAML property; null means use highest key
   * index.
   */
  static Integer resolveActiveKeyVersion(SecretsProperties props, Environment environment) {
    String raw =
        environment != null ? environment.getProperty("MAILER_SECRETS_ACTIVE_KEY_VERSION") : null;
    if (StringUtils.hasText(raw)) {
      try {
        int v = Integer.parseInt(raw.trim());
        if (v < 1 || v > SecretsKeyEnv.MAX_VERSION) {
          throw new IllegalStateException(
              "MAILER_SECRETS_ACTIVE_KEY_VERSION must be 1.."
                  + SecretsKeyEnv.MAX_VERSION
                  + " (got "
                  + v
                  + ")");
        }
        return v;
      } catch (NumberFormatException ex) {
        throw new IllegalStateException(
            "MAILER_SECRETS_ACTIVE_KEY_VERSION must be an integer (got \"" + raw + "\")");
      }
    }
    Integer fromProps = props != null ? props.getActiveKeyVersion() : null;
    if (fromProps != null && (fromProps < 1 || fromProps > SecretsKeyEnv.MAX_VERSION)) {
      throw new IllegalStateException(
          "mailer.secrets.active-key-version must be 1.."
              + SecretsKeyEnv.MAX_VERSION
              + " (got "
              + fromProps
              + ")");
    }
    return fromProps;
  }

  private static Map<Integer, String> keysFromProperties(SecretsProperties props) {
    Map<Integer, String> byVersion = new LinkedHashMap<>();
    for (SecretsProperties.KeyEntry e : props.getKeys()) {
      if (e == null) {
        throw new IllegalStateException("mailer.secrets.keys entry must not be null");
      }
      if (e.getVersion() < 1 || e.getVersion() > SecretsKeyEnv.MAX_VERSION) {
        throw new IllegalStateException(
            "mailer.secrets.keys version must be 1.."
                + SecretsKeyEnv.MAX_VERSION
                + " (got "
                + e.getVersion()
                + ")");
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
      if (version < 1 || version > SecretsKeyEnv.MAX_VERSION) {
        throw new IllegalStateException(
            "MAILER_SECRETS_KEY version must be 1.."
                + SecretsKeyEnv.MAX_VERSION
                + " (got "
                + version
                + ")");
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
