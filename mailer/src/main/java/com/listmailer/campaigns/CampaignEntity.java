package com.listmailer.campaigns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "campaigns")
public class CampaignEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 255)
  private String subject;

  @Lob
  @Column(name = "html_body", nullable = false, columnDefinition = "MEDIUMTEXT")
  private String htmlBody;

  @Lob
  @Column(name = "text_body", nullable = false, columnDefinition = "MEDIUMTEXT")
  private String textBody;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private CampaignStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "queued_at")
  private Instant queuedAt;

  protected CampaignEntity() {}

  public CampaignEntity(
      String id,
      String subject,
      String htmlBody,
      String textBody,
      CampaignStatus status,
      Instant createdAt) {
    this.id = id;
    this.subject = subject;
    this.htmlBody = htmlBody;
    this.textBody = textBody;
    this.status = status;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getHtmlBody() {
    return htmlBody;
  }

  public void setHtmlBody(String htmlBody) {
    this.htmlBody = htmlBody;
  }

  public String getTextBody() {
    return textBody;
  }

  public void setTextBody(String textBody) {
    this.textBody = textBody;
  }

  public CampaignStatus getStatus() {
    return status;
  }

  public void setStatus(CampaignStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getQueuedAt() {
    return queuedAt;
  }

  public void setQueuedAt(Instant queuedAt) {
    this.queuedAt = queuedAt;
  }
}
