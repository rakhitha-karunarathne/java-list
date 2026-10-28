package com.listmailer.secrets;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "secrets")
public class SecretEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "key_version", nullable = false)
  private int keyVersion;

  @Lob
  @Column(nullable = false, columnDefinition = "VARBINARY(2048)")
  private byte[] ciphertext;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected SecretEntity() {}

  public SecretEntity(String id, int keyVersion, byte[] ciphertext, Instant createdAt) {
    this.id = id;
    this.keyVersion = keyVersion;
    this.ciphertext = ciphertext;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public int getKeyVersion() {
    return keyVersion;
  }

  public void setKeyVersion(int keyVersion) {
    this.keyVersion = keyVersion;
  }

  public byte[] getCiphertext() {
    return ciphertext;
  }

  public void setCiphertext(byte[] ciphertext) {
    this.ciphertext = ciphertext;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
