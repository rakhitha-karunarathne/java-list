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
        ParameterKeys.SITE_TITLE,
        SiteTitles.DEFAULT,
        "Website title shown in the sidebar brand, on the sign-in screen, and as the authenticator-app name when enrolling MFA. Use a distinct value per deployment. Default List Mailer.");
    put(
        ParameterKeys.PUBLIC_BASE_URL,
        "https://lists.example.com",
        "Public HTTPS base URL for member links (pixel, unsubscribe, /dl). Must start with https://. No trailing slash required.");
    put(
        ParameterKeys.PUBLIC_DL_CACHE_MAX_AGE_HOURS,
        "24",
        "Hours browsers, CDNs, and reverse proxies may cache public campaign images from /dl (Cache-Control max-age), reducing download traffic on this server. Integer >= 1. Default 24.");
    put(
        ParameterKeys.MFA_MANDATORY,
        "false",
        "When true, all operators must enroll TOTP before a full session. Values: true|false. Default false.");
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
        ParameterKeys.BOUNCE_MESSAGES_PROCESSED_RETENTION_DAYS,
        "60",
        "Days to keep PROCESSED bounce messages (Bounce Messages page). Age is received_at. Integer >= 1. Default 60. Does not delete CORRELATED rows still awaiting process.");
    put(
        ParameterKeys.BOUNCE_MESSAGES_UNCORRELATED_RETENTION_DAYS,
        "60",
        "Days to keep UNCORRELATED bounce messages (Bounce Messages page). Age is received_at. Integer >= 1. Default 60.");
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
        ParameterKeys.SEND_RETRY_COUNT,
        "3",
        "Further SMTP tries after the first failure for a recipient. Integer >= 0. Default 3.");
    put(
        ParameterKeys.SEND_RETRY_DELAY_MINUTES,
        "5",
        "Minutes to wait before the next try of a Failed-Retry Pending row. Integer >= 0. Default 5.");
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
        ParameterKeys.CAMPAIGN_PAYLOAD_MAX_KB,
        "2000",
        "Hard upper for Campaign HTML+text payload size in KB before queue. Integer >= 1. Default 2000.");
    put(
        ParameterKeys.CAMPAIGN_IMAGE_MAX_KB,
        "500",
        "Hard upper for a single fetched/stored Campaign image in KB. Integer >= 1. Default 500.");
    put(
        ParameterKeys.CAMPAIGN_IMAGE_INLINE_MAX_KB,
        "256",
        "When Embed images in message is on, max size in KB for one image to send as CID (larger use /dl). Integer >= 1. Default 256.");
    put(
        ParameterKeys.CAMPAIGN_IMAGE_INLINE_TOTAL_MAX_KB,
        "2048",
        "When Embed images in message is on, max total KB of CID images per message (excess use /dl). Integer >= 1. Default 2048.");
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
