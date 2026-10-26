package com.listmailer.kernel.ports;

/** Declared Parameter keys (AD-15). Values are non-credential tunables only. */
public final class ParameterKeys {

  public static final String PUBLIC_BASE_URL = "public.base.url";
  public static final String MFA_MANDATORY = "operator.mfa.mandatory";
  public static final String VERP_ENABLED = "verp.enabled";
  public static final String OPS_LOG_RETENTION_DAYS = "ops.log.retention.days";
  public static final String AUDIT_RETENTION_DAYS = "audit.retention.days";
  /** Max SMTP sends per minute system-wide; 0 = unlimited (tests). */
  public static final String SEND_THROTTLE_PER_MINUTE = "send.queue.throttle.per.minute";
  public static final String IMAGE_FETCH_MODE = "campaign.image.fetch.mode";
  public static final String CAMPAIGN_PAYLOAD_MAX_BYTES = "campaign.payload.max.bytes";
  public static final String CAMPAIGN_IMAGE_MAX_BYTES = "campaign.image.max.bytes";
  public static final String BOUNCE_HARD_LIMIT = "bounce.hard.limit";
  public static final String BOUNCE_SOFT_LIMIT = "bounce.soft.limit";
  public static final String BOUNCE_HOLDBACK_MULTIPLIER = "bounce.holdback.multiplier";

  private ParameterKeys() {}
}
