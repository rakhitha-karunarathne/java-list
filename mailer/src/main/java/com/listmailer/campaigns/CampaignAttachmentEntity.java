package com.listmailer.campaigns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "campaign_attachments")
public class CampaignAttachmentEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "campaign_id", length = 36, nullable = false)
  private String campaignId;

  @Column(nullable = false, length = 255)
  private String filename;

  @Column(name = "content_type", nullable = false, length = 127)
  private String contentType;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGBLOB")
  private byte[] content;

  /** Original MIME Content-ID when ingested from inbound CID (preserved at send). */
  @Column(name = "content_id", length = 255)
  private String contentId;

  protected CampaignAttachmentEntity() {}

  public CampaignAttachmentEntity(
      String id, String campaignId, String filename, String contentType, byte[] content) {
    this(id, campaignId, filename, contentType, content, null);
  }

  public CampaignAttachmentEntity(
      String id,
      String campaignId,
      String filename,
      String contentType,
      byte[] content,
      String contentId) {
    this.id = id;
    this.campaignId = campaignId;
    this.filename = filename;
    this.contentType = contentType;
    this.content = content;
    this.contentId = contentId;
  }

  public String getId() {
    return id;
  }

  public String getCampaignId() {
    return campaignId;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentType() {
    return contentType;
  }

  public byte[] getContent() {
    return content;
  }

  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }
}
