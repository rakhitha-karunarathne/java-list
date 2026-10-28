package com.listmailer.secrets;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mailer.secrets")
public class SecretsProperties {

  /** Master keys; each entry’s {@code version} is stored on secret rows as {@code key_version}. */
  private List<KeyEntry> keys = new ArrayList<>();

  /** When true, rewrap every secret to the highest configured key version on startup. */
  private boolean migrateOnStartup = false;

  public List<KeyEntry> getKeys() {
    return keys;
  }

  public void setKeys(List<KeyEntry> keys) {
    this.keys = keys != null ? keys : new ArrayList<>();
  }

  public boolean isMigrateOnStartup() {
    return migrateOnStartup;
  }

  public void setMigrateOnStartup(boolean migrateOnStartup) {
    this.migrateOnStartup = migrateOnStartup;
  }

  public static class KeyEntry {
    private int version;
    private String key;

    public int getVersion() {
      return version;
    }

    public void setVersion(int version) {
      this.version = version;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }
  }
}
