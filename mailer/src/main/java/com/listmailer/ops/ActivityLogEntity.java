package com.listmailer.ops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "activity_logs")
public class ActivityLogEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 16)
  private String kind;

  @Column(nullable = false, length = 32)
  private String event;

  @Column(nullable = false, length = 1024)
  private String message;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ActivityLogEntity() {}

  public ActivityLogEntity(String id, String kind, String event, String message, Instant createdAt) {
    this.id = id;
    this.kind = kind;
    this.event = event == null ? "" : truncate(event, 32);
    this.message = truncate(message, 1024);
    this.createdAt = createdAt;
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    String flat = s.replace('\r', ' ').replace('\n', ' ');
    return flat.length() <= max ? flat : flat.substring(0, max);
  }

  public String getId() {
    return id;
  }

  public String getKind() {
    return kind;
  }

  public String getEvent() {
    return event;
  }

  public String getMessage() {
    return message;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
