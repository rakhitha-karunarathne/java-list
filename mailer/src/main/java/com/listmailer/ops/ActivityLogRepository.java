package com.listmailer.ops;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, String> {

  List<ActivityLogEntity> findTop500ByKindOrderByCreatedAtDesc(String kind);

  @Query(
      "select a from ActivityLogEntity a where a.kind = :kind and a.message like concat('%mailboxId=', :mailboxId, '%') order by a.createdAt desc")
  List<ActivityLogEntity> findByKindAndMailboxId(
      @Param("kind") String kind, @Param("mailboxId") String mailboxId);

  @Modifying(clearAutomatically = true)
  @Query("delete from ActivityLogEntity a where a.kind = :kind and a.createdAt < :cutoff")
  int deleteKindOlderThan(@Param("kind") String kind, @Param("cutoff") Instant cutoff);
}
