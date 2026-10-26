package com.listmailer.mts;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsDropLogRepository extends JpaRepository<MtsDropLogEntity, String> {

  List<MtsDropLogEntity> findByInboxIdOrderByCreatedAtDesc(String inboxId);

  List<MtsDropLogEntity> findAllByOrderByCreatedAtDesc();

  void deleteByInboxId(String inboxId);

  long countBySenderEmailAndCountedForAutoBlockTrueAndCreatedAtAfter(
      String senderEmail, Instant after);
}
