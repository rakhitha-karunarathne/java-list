package com.listmailer.catalogs;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogReferenceRepository extends JpaRepository<CatalogReferenceEntity, String> {

  List<CatalogReferenceEntity> findByTargetTypeAndTargetId(
      CatalogTargetType targetType, String targetId);

  boolean existsByTargetTypeAndTargetId(CatalogTargetType targetType, String targetId);
}
