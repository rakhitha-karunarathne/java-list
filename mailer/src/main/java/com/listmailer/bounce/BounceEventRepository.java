package com.listmailer.bounce;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BounceEventRepository extends JpaRepository<BounceEventEntity, String> {

  List<BounceEventEntity> findByStatus(String status);

  long countByMailboxIdAndStatus(String mailboxId, String status);
}
