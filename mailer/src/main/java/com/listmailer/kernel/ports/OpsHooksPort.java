package com.listmailer.kernel.ports;

/** Emission hooks for Ops Logs / Attention / Audit; epic-ops-attention-audit completes the UIs. */
public interface OpsHooksPort {

  void opsLog(String category, String message);

  void attentionAdmin(String title, String detail);

  /** Append-only audit trail; never pass raw secrets or full opaque tokens. */
  default void audit(String action, String detail) {
    audit(action, detail, null, null, null);
  }

  default void audit(
      String action, String detail, String actor, String listId, String memberId) {
    opsLog("audit." + action, detail);
  }
}
