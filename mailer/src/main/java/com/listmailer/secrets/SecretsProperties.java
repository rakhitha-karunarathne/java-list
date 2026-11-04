package com.listmailer.secrets;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mailer.secrets")
public class SecretsProperties {

  /**
   * Optional YAML fallback when no {@code MAILER_SECRETS_KEY_*} env is set (tests / rare). Prefer
   * env vars {@code MAILER_SECRETS_KEY_1} … {@code MAILER_SECRETS_KEY_1024}.
   */
  private List<KeyEntry> keys = new ArrayList<>();

  /**
   * Version that encrypts new secrets and is the migrate/rewrap target. Null → highest present key
   * index. Env: {@code MAILER_SECRETS_ACTIVE_KEY_VERSION}.
   */
  private Integer activeKeyVersion;

  /** When true, rewrap every secret onto the active key version on startup. */
  private boolean migrateOnStartup = false;

  public List<KeyEntry> getKeys() {
    return keys;
  }

  public void setKeys(List<KeyEntry> keys) {
    this.keys = keys != null ? keys : new ArrayList<>();
  }

  public Integer getActiveKeyVersion() {
    return activeKeyVersion;
  }

  public void setActiveKeyVersion(Integer activeKeyVersion) {
    this.activeKeyVersion = activeKeyVersion;
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
