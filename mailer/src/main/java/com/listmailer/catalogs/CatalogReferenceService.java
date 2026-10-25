package com.listmailer.catalogs;

import com.listmailer.kernel.ids.Guids;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogReferenceService {

  private final CatalogReferenceRepository references;

  public CatalogReferenceService(CatalogReferenceRepository references) {
    this.references = references;
  }

  @Transactional
  public void add(
      CatalogTargetType targetType,
      String targetId,
      String sourceType,
      String sourceId,
      String label) {
    boolean exists =
        references.findByTargetTypeAndTargetId(targetType, targetId).stream()
            .anyMatch(r -> r.getSourceType().equals(sourceType) && r.getSourceId().equals(sourceId));
    if (exists) {
      return;
    }
    try {
      references.save(
          new CatalogReferenceEntity(
              Guids.newGuid(), targetType, targetId, sourceType, sourceId, label));
    } catch (DataIntegrityViolationException ex) {
      // concurrent duplicate
    }
  }

  @Transactional
  public void remove(
      CatalogTargetType targetType, String targetId, String sourceType, String sourceId) {
    references.findByTargetTypeAndTargetId(targetType, targetId).stream()
        .filter(r -> r.getSourceType().equals(sourceType) && r.getSourceId().equals(sourceId))
        .forEach(references::delete);
  }

  public List<CatalogReferenceEntity> usedBy(CatalogTargetType targetType, String targetId) {
    return references.findByTargetTypeAndTargetId(targetType, targetId);
  }

  public boolean isReferenced(CatalogTargetType targetType, String targetId) {
    return references.existsByTargetTypeAndTargetId(targetType, targetId);
  }
}
