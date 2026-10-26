package com.listmailer.ops;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OpsLogRepository extends JpaRepository<OpsLogEntity, String> {

  List<OpsLogEntity> findAllByOrderByCreatedAtDesc();

  List<OpsLogEntity> findByCategoryOrderByCreatedAtDesc(String category);

  @Modifying(clearAutomatically = true)
  @Query("delete from OpsLogEntity o where o.createdAt < :cutoff")
  int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
