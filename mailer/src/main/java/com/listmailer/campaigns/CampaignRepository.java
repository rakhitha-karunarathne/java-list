package com.listmailer.campaigns;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRepository extends JpaRepository<CampaignEntity, String> {

  List<CampaignEntity> findByStatus(CampaignStatus status);
}
