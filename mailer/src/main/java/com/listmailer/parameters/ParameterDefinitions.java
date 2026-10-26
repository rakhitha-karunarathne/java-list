package com.listmailer.parameters;

import com.listmailer.kernel.ports.ParameterKeys;
import java.util.LinkedHashMap;
import java.util.Map;

/** Declared Parameters with seed defaults and Admin descriptions (AD-15). */
public final class ParameterDefinitions {

  public record Def(String key, String defaultValue, String description) {}

  private static final Map<String, Def> ALL = new LinkedHashMap<>();

  static {
    put(
        ParameterKeys.PUBLIC_BASE_URL,
        "https://lists.example.com",
        "Public HTTPS base URL for member links (pixel, unsubscribe, /dl). Must start with https://. No trailing slash required.");
    put(
        ParameterKeys.MFA_MANDATORY,
        "false",
        "When true, all operators must enroll TOTP before a full session. Values: true|false. Default false.");
    put(
        ParameterKeys.VERP_ENABLED,
        "false",
        "Enable Variable Envelope Return Path for bounce correlation. Values: true|false. Default false.");
    put(
        ParameterKeys.OPS_LOG_RETENTION_DAYS,
        "90",
        "Days to retain ops logs (send/bounce/MTS). Integer >= 1. Default 90.");
    put(
        ParameterKeys.AUDIT_RETENTION_DAYS,
        "365",
        "Days to retain audit records. Integer >= 1. Default 365.");
    put(
        ParameterKeys.SEND_THROTTLE_PER_MINUTE,
        "120",
        "System-wide max Campaign SMTP sends per minute. Integer >= 0 (0 = unlimited). Default 120.");
  }

  private ParameterDefinitions() {}

  private static void put(String key, String def, String description) {
    ALL.put(key, new Def(key, def, description));
  }

  public static Map<String, Def> all() {
    return ALL;
  }

  public static Def require(String key) {
    Def def = ALL.get(key);
    if (def == null) {
      throw new IllegalArgumentException("unknown parameter: " + key);
    }
    return def;
  }
}
