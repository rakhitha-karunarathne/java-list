package com.listmailer.campaigns;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignOpenRepository extends JpaRepository<CampaignOpenEntity, String> {

  List<CampaignOpenEntity> findByCampaignId(String campaignId);

  long countByCampaignId(String campaignId);
}
