package com.listmailer.campaigns;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignListRepository extends JpaRepository<CampaignListEntity, CampaignListEntity.Pk> {

  List<CampaignListEntity> findByCampaignId(String campaignId);
}
