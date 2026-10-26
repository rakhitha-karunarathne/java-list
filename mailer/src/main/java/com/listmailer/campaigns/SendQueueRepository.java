package com.listmailer.campaigns;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SendQueueRepository extends JpaRepository<SendQueueEntity, String> {

  List<SendQueueEntity> findByCampaignId(String campaignId);

  List<SendQueueEntity> findByStatus(String status);

  Optional<SendQueueEntity> findByPixelToken(String pixelToken);

  Optional<SendQueueEntity> findByCorrelationToken(String correlationToken);

  Optional<SendQueueEntity> findBySmtpMessageId(String smtpMessageId);

  Optional<SendQueueEntity> findByVerpRcpt(String verpRcpt);

  List<SendQueueEntity> findByMemberIdAndStatus(String memberId, String status);

  List<SendQueueEntity> findByStatusAndSubjectIgnoreCase(String status, String subject);

  long countByStatusAndSentAtAfter(String status, java.time.Instant sentAt);
}
