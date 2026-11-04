package com.listmailer.secrets;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * Loads secret master keys from {@code MAILER_SECRETS_KEY_<version>} (version 1…{@link #MAX_VERSION}).
 * Keep every env var still referenced by rows until after a successful rewrap to the active version.
 */
public final class SecretsKeyEnv {

  /** Highest version number scanned (long-lived installs; gaps allowed). */
  static final int MAX_VERSION = 1024;

  private SecretsKeyEnv() {}

  /**
   * Returns version → key material for every non-blank {@code MAILER_SECRETS_KEY_n} found.
   * Versions are ordered ascending.
   */
  public static Map<Integer, String> read(Environment environment) {
    TreeMap<Integer, String> found = new TreeMap<>();
    if (environment == null) {
      return Map.copyOf(found);
    }
    for (int version = 1; version <= MAX_VERSION; version++) {
      String value = environment.getProperty("MAILER_SECRETS_KEY_" + version);
      if (value != null) {
        value = value.trim();
      }
      if (StringUtils.hasText(value)) {
        found.put(version, value);
      }
    }
    return new LinkedHashMap<>(found);
  }
}
