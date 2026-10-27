package com.listmailer.lists;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "lists")
public class ListEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "sending_path_id", nullable = false, length = 36)
  private String sendingPathId;

  @Column(name = "footer_id", length = 36)
  private String footerId;

  @Column(name = "spam_threshold", precision = 6, scale = 3)
  private BigDecimal spamThreshold;

  @Column(name = "payload_override", length = 64)
  private String payloadOverride;

  @Column(name = "image_ttl_override_hours")
  private Integer imageTtlOverrideHours;

  @Column(nullable = false)
  private boolean paused;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ListEntity() {}

  public ListEntity(
      String id,
      String name,
      String sendingPathId,
      String footerId,
      BigDecimal spamThreshold,
      String payloadOverride,
      Integer imageTtlOverrideHours,
      Instant createdAt) {
    this.id = id;
    this.name = name;
    this.sendingPathId = sendingPathId;
    this.footerId = footerId;
    this.spamThreshold = spamThreshold;
    this.payloadOverride = payloadOverride;
    this.imageTtlOverrideHours = imageTtlOverrideHours;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSendingPathId() {
    return sendingPathId;
  }

  public void setSendingPathId(String sendingPathId) {
    this.sendingPathId = sendingPathId;
  }

  public String getFooterId() {
    return footerId;
  }

  public void setFooterId(String footerId) {
    this.footerId = footerId;
  }

  public BigDecimal getSpamThreshold() {
    return spamThreshold;
  }

  public void setSpamThreshold(BigDecimal spamThreshold) {
    this.spamThreshold = spamThreshold;
  }

  public String getPayloadOverride() {
    return payloadOverride;
  }

  public void setPayloadOverride(String payloadOverride) {
    this.payloadOverride = payloadOverride;
  }

  public Integer getImageTtlOverrideHours() {
    return imageTtlOverrideHours;
  }

  public void setImageTtlOverrideHours(Integer imageTtlOverrideHours) {
    this.imageTtlOverrideHours = imageTtlOverrideHours;
  }

  public boolean isPaused() {
    return paused;
  }

  public void setPaused(boolean paused) {
    this.paused = paused;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
