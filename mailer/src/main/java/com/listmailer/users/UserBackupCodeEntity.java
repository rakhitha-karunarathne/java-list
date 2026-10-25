package com.listmailer.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "user_backup_codes")
public class UserBackupCodeEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "user_id", length = 36, nullable = false)
  private String userId;

  @Column(name = "code_hash", nullable = false, length = 100)
  private String codeHash;

  @Column(name = "used_at")
  private Instant usedAt;

  protected UserBackupCodeEntity() {}

  public UserBackupCodeEntity(String id, String userId, String codeHash) {
    this.id = id;
    this.userId = userId;
    this.codeHash = codeHash;
  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getCodeHash() {
    return codeHash;
  }

  public Instant getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(Instant usedAt) {
    this.usedAt = usedAt;
  }

  public boolean isUsed() {
    return usedAt != null;
  }
}
