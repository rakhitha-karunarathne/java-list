package com.listmailer.catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalog_references")
public class CatalogReferenceEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 32)
  private CatalogTargetType targetType;

  @Column(name = "target_id", nullable = false, length = 36)
  private String targetId;

  @Column(name = "source_type", nullable = false, length = 32)
  private String sourceType;

  @Column(name = "source_id", nullable = false, length = 36)
  private String sourceId;

  @Column(nullable = false, length = 255)
  private String label;

  protected CatalogReferenceEntity() {}

  public CatalogReferenceEntity(
      String id,
      CatalogTargetType targetType,
      String targetId,
      String sourceType,
      String sourceId,
      String label) {
    this.id = id;
    this.targetType = targetType;
    this.targetId = targetId;
    this.sourceType = sourceType;
    this.sourceId = sourceId;
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public CatalogTargetType getTargetType() {
    return targetType;
  }

  public String getTargetId() {
    return targetId;
  }

  public String getSourceType() {
    return sourceType;
  }

  public String getSourceId() {
    return sourceId;
  }

  public String getLabel() {
    return label;
  }
}
