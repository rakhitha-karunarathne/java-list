package com.listmailer.bounce;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BounceEventRepository extends JpaRepository<BounceEventEntity, String> {

  List<BounceEventEntity> findByStatus(String status);

  @Query("select e.id from BounceEventEntity e where e.status = :status")
  List<String> findIdsByStatus(@Param("status") String status);

  List<BounceEventEntity> findByStatusAndMemberId(String status, String memberId);

  List<BounceEventEntity> findAllByOrderByReceivedAtDesc();

  long countByMailboxIdAndStatus(String mailboxId, String status);

  long countBySendQueueIdIn(java.util.Collection<String> sendQueueIds);

  List<BounceEventEntity> findBySendQueueIdIn(java.util.Collection<String> sendQueueIds);

  void deleteBySendQueueIdIn(java.util.Collection<String> sendQueueIds);

  void deleteByMemberId(String memberId);

  @Modifying(clearAutomatically = true)
  @Query(
      "delete from BounceEventEntity e where e.status = :status and e.receivedAt < :cutoff")
  int deleteByStatusAndReceivedAtBefore(
      @Param("status") String status, @Param("cutoff") Instant cutoff);
}
