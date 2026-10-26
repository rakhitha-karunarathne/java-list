package com.listmailer.bounce;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BounceEventRepository extends JpaRepository<BounceEventEntity, String> {

  List<BounceEventEntity> findByStatus(String status);

  List<BounceEventEntity> findByStatusAndMemberId(String status, String memberId);

  long countByMailboxIdAndStatus(String mailboxId, String status);

  long countBySendQueueIdIn(java.util.Collection<String> sendQueueIds);
}
