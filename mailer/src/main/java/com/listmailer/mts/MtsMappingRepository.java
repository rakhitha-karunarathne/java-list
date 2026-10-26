package com.listmailer.mts;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsMappingRepository extends JpaRepository<MtsMappingEntity, String> {

  List<MtsMappingEntity> findByInboxId(String inboxId);

  List<MtsMappingEntity> findByFooterId(String footerId);
}
