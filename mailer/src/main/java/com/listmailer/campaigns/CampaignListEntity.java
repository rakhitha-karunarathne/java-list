package com.listmailer.campaigns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "campaign_lists")
@IdClass(CampaignListEntity.Pk.class)
public class CampaignListEntity {

  @Id
  @Column(name = "campaign_id", length = 36, nullable = false)
  private String campaignId;

  @Id
  @Column(name = "list_id", length = 36, nullable = false)
  private String listId;

  protected CampaignListEntity() {}

  public CampaignListEntity(String campaignId, String listId) {
    this.campaignId = campaignId;
    this.listId = listId;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public String getListId() {
    return listId;
  }

  public static final class Pk implements Serializable {
    private String campaignId;
    private String listId;

    public Pk() {}

    public String getCampaignId() {
      return campaignId;
    }

    public void setCampaignId(String campaignId) {
      this.campaignId = campaignId;
    }

    public String getListId() {
      return listId;
    }

    public void setListId(String listId) {
      this.listId = listId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Pk pk)) {
        return false;
      }
      return Objects.equals(campaignId, pk.campaignId) && Objects.equals(listId, pk.listId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(campaignId, listId);
    }
  }
}
