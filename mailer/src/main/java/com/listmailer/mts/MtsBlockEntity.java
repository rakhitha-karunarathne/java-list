package com.listmailer.mts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mts_blocks")
public class MtsBlockEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  /** SENDER or DOMAIN */
  @Column(nullable = false, length = 16)
  private String kind;

  @Column(nullable = false, length = 320)
  private String value;

  @Column(nullable = false, length = 32)
  private String source;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected MtsBlockEntity() {}

  public MtsBlockEntity(String id, String kind, String value, String source, Instant createdAt) {
    this.id = id;
    this.kind = kind;
    this.value = value;
    this.source = source;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getKind() {
    return kind;
  }

  public String getValue() {
    return value;
  }

  public String getSource() {
    return source;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
