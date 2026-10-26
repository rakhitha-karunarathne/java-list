package com.listmailer.ops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "ops_logs")
public class OpsLogEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 64)
  private String category;

  @Column(nullable = false, length = 16)
  private String severity;

  @Column(nullable = false, length = 1024)
  private String message;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected OpsLogEntity() {}

  public OpsLogEntity(
      String id, String category, String severity, String message, Instant createdAt) {
    this.id = id;
    this.category = category;
    this.severity = severity;
    this.message = truncate(message, 1024);
    this.createdAt = createdAt;
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    return s.length() <= max ? s : s.substring(0, max);
  }

  public String getId() {
    return id;
  }

  public String getCategory() {
    return category;
  }

  public String getSeverity() {
    return severity;
  }

  public String getMessage() {
    return message;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
