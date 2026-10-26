package com.listmailer.campaigns;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignAttachmentRepository
    extends JpaRepository<CampaignAttachmentEntity, String> {

  List<CampaignAttachmentEntity> findByCampaignId(String campaignId);

  void deleteByCampaignId(String campaignId);
}
