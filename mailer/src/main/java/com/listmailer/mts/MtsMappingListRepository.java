package com.listmailer.mts;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsMappingListRepository
    extends JpaRepository<MtsMappingListEntity, MtsMappingListEntity.Pk> {

  List<MtsMappingListEntity> findByMappingIdOrderByPositionAsc(String mappingId);

  void deleteByMappingId(String mappingId);
}
