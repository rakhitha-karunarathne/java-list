package com.listmailer.kernel.ports;

/** Declared Parameter keys (AD-15). Values are non-credential tunables only. */
public final class ParameterKeys {

  public static final String PUBLIC_BASE_URL = "public.base.url";
  public static final String MFA_MANDATORY = "operator.mfa.mandatory";
  public static final String VERP_ENABLED = "verp.enabled";
  public static final String OPS_LOG_RETENTION_DAYS = "ops.log.retention.days";
  public static final String AUDIT_RETENTION_DAYS = "audit.retention.days";

  private ParameterKeys() {}
}
