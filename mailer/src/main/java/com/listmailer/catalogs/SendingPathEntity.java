package com.listmailer.catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "sending_paths")
public class SendingPathEntity {

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

  @Column(name = "password_secret_id", nullable = false, length = 36)
  private String passwordSecretId;

  @Column(name = "from_email", nullable = false, length = 320)
  private String fromEmail;

  @Column(name = "from_name", nullable = false, length = 120)
  private String fromName;

  @Column(name = "return_path_email", length = 320)
  private String returnPathEmail;

  @Column(name = "use_tls", nullable = false)
  private boolean useTls;

  @Column(name = "use_ssl", nullable = false)
  private boolean useSsl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected SendingPathEntity() {}

  public SendingPathEntity(
      String id,
      String name,
      String host,
      int port,
      String username,
      String passwordSecretId,
      String fromEmail,
      String fromName,
      boolean useTls,
      Instant createdAt) {
    this.id = id;
    this.name = name;
    this.host = host;
    this.port = port;
    this.username = username;
    this.passwordSecretId = passwordSecretId;
    this.fromEmail = fromEmail;
    this.fromName = fromName;
    this.useTls = useTls;
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

  public String getPasswordSecretId() {
    return passwordSecretId;
  }

  public void setPasswordSecretId(String passwordSecretId) {
    this.passwordSecretId = passwordSecretId;
  }

  public String getFromEmail() {
    return fromEmail;
  }

  public void setFromEmail(String fromEmail) {
    this.fromEmail = fromEmail;
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }

  public String getReturnPathEmail() {
    return returnPathEmail;
  }

  public void setReturnPathEmail(String returnPathEmail) {
    this.returnPathEmail = returnPathEmail;
  }

  public boolean isUseTls() {
    return useTls;
  }

  public void setUseTls(boolean useTls) {
    this.useTls = useTls;
  }

  public boolean isUseSsl() {
    return useSsl;
  }

  public void setUseSsl(boolean useSsl) {
    this.useSsl = useSsl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
