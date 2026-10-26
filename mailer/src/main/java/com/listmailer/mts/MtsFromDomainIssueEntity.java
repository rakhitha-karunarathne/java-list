package com.listmailer.mts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mts_from_domain_issues")
public class MtsFromDomainIssueEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "inbox_id", length = 36, nullable = false)
  private String inboxId;

  @Column(name = "mapping_id", length = 36)
  private String mappingId;

  @Column(name = "sender_email", length = 320, nullable = false)
  private String senderEmail;

  @Column(length = 998)
  private String subject;

  @Column(nullable = false, length = 32)
  private String status;

  @Column(length = 32)
  private String resolution;

  @Column(nullable = false, length = 512)
  private String detail;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected MtsFromDomainIssueEntity() {}

  public MtsFromDomainIssueEntity(
      String id,
      String inboxId,
      String mappingId,
      String senderEmail,
      String subject,
      String status,
      String resolution,
      String detail,
      Instant createdAt) {
    this.id = id;
    this.inboxId = inboxId;
    this.mappingId = mappingId;
    this.senderEmail = senderEmail;
    this.subject = subject;
    this.status = status;
    this.resolution = resolution;
    this.detail = detail;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getInboxId() {
    return inboxId;
  }

  public String getMappingId() {
    return mappingId;
  }

  public String getSenderEmail() {
    return senderEmail;
  }

  public String getSubject() {
    return subject;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getResolution() {
    return resolution;
  }

  public String getDetail() {
    return detail;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
