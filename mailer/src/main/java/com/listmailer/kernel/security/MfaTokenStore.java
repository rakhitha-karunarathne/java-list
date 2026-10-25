package com.listmailer.kernel.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Single-use MFA step-up tokens (jti). shortcut: in-memory only, multi-node needs shared store. */
@Component
public class MfaTokenStore {

  private final Map<String, Long> used = new ConcurrentHashMap<>();

  public boolean consume(String jti) {
    prune();
    return used.putIfAbsent(jti, System.currentTimeMillis() + 300_000L) == null;
  }

  private void prune() {
    long now = System.currentTimeMillis();
    used.entrySet().removeIf(e -> e.getValue() < now);
  }
}
