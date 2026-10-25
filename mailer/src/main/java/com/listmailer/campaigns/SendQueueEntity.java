package com.listmailer.campaigns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "send_queue")
public class SendQueueEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "campaign_id", nullable = false, length = 36)
  private String campaignId;

  @Column(name = "member_id", nullable = false, length = 36)
  private String memberId;

  @Column(nullable = false, length = 320)
  private String email;

  @Column(name = "sending_path_id", nullable = false, length = 36)
  private String sendingPathId;

  @Column(nullable = false, length = 32)
  private String status;

  protected SendQueueEntity() {}

  public SendQueueEntity(
      String id,
      String campaignId,
      String memberId,
      String email,
      String sendingPathId,
      String status) {
    this.id = id;
    this.campaignId = campaignId;
    this.memberId = memberId;
    this.email = email;
    this.sendingPathId = sendingPathId;
    this.status = status;
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

  public String getEmail() {
    return email;
  }

  public String getSendingPathId() {
    return sendingPathId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
