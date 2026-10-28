package com.listmailer.secrets;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mailer.secrets")
public class SecretsProperties {

  /** version → key material (≥32 bytes). Highest version is current. */
  private Map<Integer, String> keys = new LinkedHashMap<>();

  public Map<Integer, String> getKeys() {
    return keys;
  }

  public void setKeys(Map<Integer, String> keys) {
    this.keys = keys != null ? keys : new LinkedHashMap<>();
  }
}
