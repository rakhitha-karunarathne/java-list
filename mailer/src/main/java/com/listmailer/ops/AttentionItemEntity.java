package com.listmailer.ops;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "attention_items")
public class AttentionItemEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(nullable = false, length = 1024)
  private String detail;

  /** OPS_ERROR or GENERIC */
  @Column(nullable = false, length = 32)
  private String kind;

  @Column(name = "list_id", length = 36)
  private String listId;

  @Column(name = "open_flag", nullable = false)
  private boolean open;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AttentionItemEntity() {}

  public AttentionItemEntity(
      String id,
      String title,
      String detail,
      String kind,
      String listId,
      Instant createdAt) {
    this.id = id;
    this.title = title;
    this.detail = detail == null ? "" : (detail.length() <= 1024 ? detail : detail.substring(0, 1024));
    this.kind = kind;
    this.listId = listId;
    this.open = true;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }

  public String getKind() {
    return kind;
  }

  public String getListId() {
    return listId;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
