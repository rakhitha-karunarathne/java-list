package com.listmailer.campaigns;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SendQueueRepository extends JpaRepository<SendQueueEntity, String> {

  List<SendQueueEntity> findByCampaignId(String campaignId);

  List<SendQueueEntity> findByStatus(String status);

  Optional<SendQueueEntity> findByPixelToken(String pixelToken);
}
