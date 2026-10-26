package com.listmailer.campaigns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "campaign_opens")
public class CampaignOpenEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "campaign_id", length = 36, nullable = false)
  private String campaignId;

  @Column(name = "member_id", length = 36, nullable = false)
  private String memberId;

  @Column(name = "send_queue_id", length = 36, nullable = false)
  private String sendQueueId;

  @Column(name = "opened_at", nullable = false)
  private Instant openedAt;

  protected CampaignOpenEntity() {}

  public CampaignOpenEntity(
      String id, String campaignId, String memberId, String sendQueueId, Instant openedAt) {
    this.id = id;
    this.campaignId = campaignId;
    this.memberId = memberId;
    this.sendQueueId = sendQueueId;
    this.openedAt = openedAt;
  }

  public String getId() {
    return id;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public String getMemberId() {
    return memberId;
  }

  public String getSendQueueId() {
    return sendQueueId;
  }

  public Instant getOpenedAt() {
    return openedAt;
  }
}
