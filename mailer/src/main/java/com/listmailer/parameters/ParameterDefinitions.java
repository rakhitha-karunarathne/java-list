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
        "Days to retain ops logs (send/bounce/MTS errors). Integer >= 1. Default 90.");
    put(
        ParameterKeys.BOUNCE_PROCESS_LOG_RETENTION_DAYS,
        "30",
        "Days to retain the Bounce Processing Log. Integer >= 1. Default 30.");
    put(
        ParameterKeys.BOUNCE_PROCESS_LOG_MAX_ROWS,
        "5000",
        "Maximum Bounce Processing Log rows to keep. Integer >= 1. Default 5000.");
    put(
        ParameterKeys.BOUNCE_PROCESS_LOG_ENABLED,
        "true",
        "Write the Bounce Processing Log. Values: true|false. Default true.");
    put(
        ParameterKeys.SEND_QUEUE_LOG_RETENTION_DAYS,
        "30",
        "Days to retain the Queue Sending Log. Integer >= 1. Default 30.");
    put(
        ParameterKeys.SEND_QUEUE_LOG_MAX_ROWS,
        "5000",
        "Maximum Queue Sending Log rows to keep. Integer >= 1. Default 5000.");
    put(
        ParameterKeys.SEND_QUEUE_LOG_ENABLED,
        "true",
        "Write the Queue Sending Log. Values: true|false. Default true.");
    put(
        ParameterKeys.MTS_PROCESS_LOG_RETENTION_DAYS,
        "30",
        "Days to retain the Mail-To-Send Processing Log. Integer >= 1. Default 30.");
    put(
        ParameterKeys.MTS_PROCESS_LOG_MAX_ROWS,
        "5000",
        "Maximum Mail-To-Send Processing Log rows to keep. Integer >= 1. Default 5000.");
    put(
        ParameterKeys.MTS_PROCESS_LOG_ENABLED,
        "true",
        "Write the Mail-To-Send Processing Log. Values: true|false. Default true.");
    put(
        ParameterKeys.AUDIT_RETENTION_DAYS,
        "365",
        "Days to retain audit records. Integer >= 1. Default 365.");
    put(
        ParameterKeys.SEND_PAUSED,
        "false",
        "When true, the send-queue processor starts no further campaign messages. Test sends and Notice-path mail still go out. Values: true|false. Default false.");
    put(
        ParameterKeys.SEND_DELAY_PER_EMAIL_MS,
        "1000",
        "Pause after each SMTP send, in milliseconds. Integer >= 0. Default 1000.");
    put(
        ParameterKeys.SEND_BATCH_SIZE,
        "600",
        "Maximum SMTP sends in one schedule run. Integer >= 1. Default 600.");
    put(
        ParameterKeys.SEND_BATCH_DURATION_MINUTES,
        "10",
        "Stop the schedule run after this many minutes even if the batch is not full. Integer >= 1. Default 10.");
    put(
        ParameterKeys.SEND_MESSAGES_PER_HOUR,
        "3600",
        "Maximum messages sent in the last 60 minutes. A run sends only the remaining room, and never more than the batch size. Integer >= 1. Default 3600.");
    put(
        ParameterKeys.IMAGE_FETCH_MODE,
        "Off",
        "Remote image fetch on Campaign queue: Off | Best Effort | Mandatory. Default Off.");
    put(
        ParameterKeys.CAMPAIGN_PAYLOAD_MAX_BYTES,
        "2000000",
        "Hard upper for Campaign HTML+text payload bytes before queue. Integer >= 1. Default 2000000.");
    put(
        ParameterKeys.CAMPAIGN_IMAGE_MAX_BYTES,
        "512000",
        "Hard upper for a single fetched/stored Campaign image. Integer >= 1. Default 512000.");
    put(
        ParameterKeys.BOUNCE_HARD_LIMIT,
        "3",
        "Consecutive hard bounce limit before Member Disabled. Integer >= 1. Default 3.");
    put(
        ParameterKeys.BOUNCE_SOFT_LIMIT,
        "6",
        "Consecutive soft bounce limit before Member Disabled. Integer >= 1. Seeded 2× hard; independent after. Default 6.");
    put(
        ParameterKeys.BOUNCE_HOLDBACK_MULTIPLIER,
        "2",
        "Holdback = multiplier × class limit newest sends when below limit. Integer >= 1. Default 2.");
    put(
        "mts.auto.block.per.day",
        "10",
        "Counted MTS Drop log rejects per sender per day before auto-block. Integer >= 1. Default 10.");
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
