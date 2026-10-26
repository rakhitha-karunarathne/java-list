package com.listmailer.mts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mts_drop_log")
public class MtsDropLogEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "inbox_id", length = 36)
  private String inboxId;

  @Column(name = "sender_email", length = 320)
  private String senderEmail;

  @Column(nullable = false, length = 128)
  private String reason;

  @Column(name = "counted_for_auto_block", nullable = false)
  private boolean countedForAutoBlock;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private boolean resolved;

  protected MtsDropLogEntity() {}

  public MtsDropLogEntity(
      String id,
      String inboxId,
      String senderEmail,
      String reason,
      boolean countedForAutoBlock,
      Instant createdAt) {
    this.id = id;
    this.inboxId = inboxId;
    this.senderEmail = senderEmail;
    this.reason = reason;
    this.countedForAutoBlock = countedForAutoBlock;
    this.createdAt = createdAt;
    this.resolved = false;
  }

  public String getId() {
    return id;
  }

  public String getInboxId() {
    return inboxId;
  }

  public String getSenderEmail() {
    return senderEmail;
  }

  public String getReason() {
    return reason;
  }

  public boolean isCountedForAutoBlock() {
    return countedForAutoBlock;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public boolean isResolved() {
    return resolved;
  }

  public void setResolved(boolean resolved) {
    this.resolved = resolved;
  }
}
