package com.listmailer.campaigns;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignListRepository extends JpaRepository<CampaignListEntity, CampaignListEntity.Pk> {

  List<CampaignListEntity> findByCampaignIdOrderByPositionAscListIdAsc(String campaignId);

  List<CampaignListEntity> findByListId(String listId);

  void deleteByCampaignId(String campaignId);

  void deleteByListId(String listId);
}
