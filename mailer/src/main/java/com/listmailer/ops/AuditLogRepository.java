package com.listmailer.ops;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {

  List<AuditLogEntity> findAllByOrderByCreatedAtDesc();

  List<AuditLogEntity> findByListIdInOrderByCreatedAtDesc(List<String> listIds);

  List<AuditLogEntity> findByMemberIdInOrderByCreatedAtDesc(List<String> memberIds);

  @Modifying(clearAutomatically = true)
  @Query("delete from AuditLogEntity a where a.createdAt < :cutoff")
  int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
