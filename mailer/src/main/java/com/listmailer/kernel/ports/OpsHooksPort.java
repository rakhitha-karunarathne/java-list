package com.listmailer.kernel.ports;

/** Emission hooks for Ops Logs / Attention; epic-ops-attention-audit completes the UIs. */
public interface OpsHooksPort {

  void opsLog(String category, String message);

  void attentionAdmin(String title, String detail);
}
