package com.listmailer.kernel.ports;

/** Declared Parameter keys (AD-15). Values are non-credential tunables only. */
public final class ParameterKeys {

  public static final String PUBLIC_BASE_URL = "public.base.url";
  public static final String MFA_MANDATORY = "operator.mfa.mandatory";
  public static final String VERP_ENABLED = "verp.enabled";
  public static final String OPS_LOG_RETENTION_DAYS = "ops.log.retention.days";
  public static final String BOUNCE_PROCESS_LOG_RETENTION_DAYS = "bounce.process.log.retention.days";
  public static final String BOUNCE_PROCESS_LOG_ENABLED = "bounce.process.log.enabled";
  public static final String SEND_QUEUE_LOG_RETENTION_DAYS = "send.queue.log.retention.days";
  public static final String SEND_QUEUE_LOG_ENABLED = "send.queue.log.enabled";
  public static final String MTS_PROCESS_LOG_RETENTION_DAYS = "mts.process.log.retention.days";
  public static final String MTS_PROCESS_LOG_ENABLED = "mts.process.log.enabled";
  public static final String AUDIT_RETENTION_DAYS = "audit.retention.days";
  public static final String SEND_DELAY_PER_EMAIL_MS = "send.delay.per.email.ms";
  public static final String SEND_BATCH_SIZE = "send.batch.size";
  public static final String SEND_BATCH_DURATION_MINUTES = "send.batch.duration.minutes";
  public static final String SEND_MESSAGES_PER_HOUR = "send.messages.per.hour";
  public static final String IMAGE_FETCH_MODE = "campaign.image.fetch.mode";
  public static final String CAMPAIGN_PAYLOAD_MAX_BYTES = "campaign.payload.max.bytes";
  public static final String CAMPAIGN_IMAGE_MAX_BYTES = "campaign.image.max.bytes";
  public static final String BOUNCE_HARD_LIMIT = "bounce.hard.limit";
  public static final String BOUNCE_SOFT_LIMIT = "bounce.soft.limit";
  public static final String BOUNCE_HOLDBACK_MULTIPLIER = "bounce.holdback.multiplier";

  private ParameterKeys() {}
}
