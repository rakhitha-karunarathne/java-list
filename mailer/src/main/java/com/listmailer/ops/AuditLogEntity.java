package com.listmailer.ops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 64)
  private String action;

  @Column(nullable = false, length = 1024)
  private String detail;

  @Column(length = 320)
  private String actor;

  @Column(name = "list_id", length = 36)
  private String listId;

  @Column(name = "member_id", length = 36)
  private String memberId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AuditLogEntity() {}

  public AuditLogEntity(
      String id,
      String action,
      String detail,
      String actor,
      String listId,
      String memberId,
      Instant createdAt) {
    this.id = id;
    this.action = action;
    this.detail =
        detail == null ? "" : (detail.length() <= 1024 ? detail : detail.substring(0, 1024));
    this.actor = actor;
    this.listId = listId;
    this.memberId = memberId;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getAction() {
    return action;
  }

  public String getDetail() {
    return detail;
  }

  public String getActor() {
    return actor;
  }

  public String getListId() {
    return listId;
  }

  public String getMemberId() {
    return memberId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
