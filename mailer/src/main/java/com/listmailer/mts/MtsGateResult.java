package com.listmailer.mts;

/** Outcome of auth/spam/from-domain gates before Campaign create. */
public enum MtsGateResult {
  ACCEPT,
  DROP_SILENT,
  DROP_COUNTED,
  NOTIFY_CROSS_INBOX,
  FROM_DOMAIN_ISSUE
}
