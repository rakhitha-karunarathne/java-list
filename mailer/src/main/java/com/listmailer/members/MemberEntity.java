package com.listmailer.members;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "members")
public class MemberEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 320, unique = true)
  private String email;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "disable_reason", length = 255)
  private String disableReason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected MemberEntity() {}

  public MemberEntity(String id, String email, boolean enabled, Instant createdAt) {
    this.id = id;
    this.email = email;
    this.enabled = enabled;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDisableReason() {
    return disableReason;
  }

  public void setDisableReason(String disableReason) {
    this.disableReason = disableReason;
  }
}
