package com.listmailer.secrets;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secrets")
public class SecretsAdminController {

  private final SecretStore store;

  public SecretsAdminController(SecretStore store) {
    this.store = store;
  }

  @GetMapping("/status")
  public Map<String, Object> status() {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("currentKeyVersion", store.currentKeyVersion());
    out.put("outdatedCount", store.outdatedCount());
    return out;
  }

  @PostMapping("/rewrap")
  public Map<String, Object> rewrap() {
    SecretStore.RewrapResult r = store.rewrapAllToLatest();
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("migrated", r.migrated());
    out.put("alreadyCurrent", r.alreadyCurrent());
    out.put("failed", r.failed());
    out.put("currentKeyVersion", r.currentKeyVersion());
    return out;
  }
}
