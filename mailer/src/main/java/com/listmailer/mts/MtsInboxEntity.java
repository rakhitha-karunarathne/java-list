package com.listmailer.mts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mts_inboxes")
public class MtsInboxEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 120, unique = true)
  private String name;

  @Column(nullable = false, length = 255)
  private String host;

  @Column(nullable = false)
  private int port;

  @Column(nullable = false, length = 255)
  private String username;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(name = "use_ssl", nullable = false)
  private boolean useSsl;

  @Column(name = "use_tls", nullable = false)
  private boolean useTls;

  @Column(name = "poll_interval_minutes", nullable = false)
  private int pollIntervalMinutes;

  @Column(name = "last_polled_at")
  private Instant lastPolledAt;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected MtsInboxEntity() {}

  public MtsInboxEntity(
      String id,
      String name,
      String host,
      int port,
      String username,
      String password,
      boolean useSsl,
      int pollIntervalMinutes,
      Instant createdAt) {
    this.id = id;
    this.name = name;
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.useSsl = useSsl;
    this.pollIntervalMinutes = pollIntervalMinutes;
    this.enabled = true;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isUseSsl() {
    return useSsl;
  }

  public void setUseSsl(boolean useSsl) {
    this.useSsl = useSsl;
  }

  public boolean isUseTls() {
    return useTls;
  }

  public void setUseTls(boolean useTls) {
    this.useTls = useTls;
  }

  public int getPollIntervalMinutes() {
    return pollIntervalMinutes;
  }

  public void setPollIntervalMinutes(int pollIntervalMinutes) {
    this.pollIntervalMinutes = pollIntervalMinutes;
  }

  public Instant getLastPolledAt() {
    return lastPolledAt;
  }

  public void setLastPolledAt(Instant lastPolledAt) {
    this.lastPolledAt = lastPolledAt;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
