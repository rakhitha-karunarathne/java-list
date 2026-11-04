package com.listmailer.campaigns;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SendQueueRepository extends JpaRepository<SendQueueEntity, String> {

  List<SendQueueEntity> findByCampaignId(String campaignId);

  void deleteByCampaignId(String campaignId);

  List<SendQueueEntity> findByStatus(String status);

  Optional<SendQueueEntity> findByPixelToken(String pixelToken);

  Optional<SendQueueEntity> findByCorrelationToken(String correlationToken);

  Optional<SendQueueEntity> findBySmtpMessageId(String smtpMessageId);

  Optional<SendQueueEntity> findByVerpRcpt(String verpRcpt);

  List<SendQueueEntity> findByMemberId(String memberId);

  List<SendQueueEntity> findByMemberIdAndStatus(String memberId, String status);

  void deleteByMemberId(String memberId);

  /** Relies on MariaDB CI collation so the (status, subject) index can be used. */
  List<SendQueueEntity> findByStatusAndSubject(String status, String subject);

  long countByStatusAndSentAtAfter(String status, java.time.Instant sentAt);
}
