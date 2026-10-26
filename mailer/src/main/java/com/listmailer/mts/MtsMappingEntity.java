package com.listmailer.mts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mts_mappings")
public class MtsMappingEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "inbox_id", length = 36, nullable = false)
  private String inboxId;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "substitute_from", length = 320)
  private String substituteFrom;

  @Column(name = "footer_id", length = 36)
  private String footerId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected MtsMappingEntity() {}

  public MtsMappingEntity(
      String id,
      String inboxId,
      String name,
      String substituteFrom,
      String footerId,
      Instant createdAt) {
    this.id = id;
    this.inboxId = inboxId;
    this.name = name;
    this.substituteFrom = substituteFrom;
    this.footerId = footerId;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getInboxId() {
    return inboxId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSubstituteFrom() {
    return substituteFrom;
  }

  public void setSubstituteFrom(String substituteFrom) {
    this.substituteFrom = substituteFrom;
  }

  public String getFooterId() {
    return footerId;
  }

  public void setFooterId(String footerId) {
    this.footerId = footerId;
  }
}
