package com.listmailer.campaigns;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignAttachmentRepository
    extends JpaRepository<CampaignAttachmentEntity, String> {

  List<CampaignAttachmentEntity> findByCampaignId(String campaignId);

  Optional<CampaignAttachmentEntity> findByCampaignIdAndFilename(String campaignId, String filename);

  Optional<CampaignAttachmentEntity> findByIdAndCampaignId(String id, String campaignId);

  void deleteByCampaignId(String campaignId);
}
