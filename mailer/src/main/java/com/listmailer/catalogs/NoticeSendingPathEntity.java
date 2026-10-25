package com.listmailer.catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notice_sending_path")
public class NoticeSendingPathEntity {

  @Id
  @Column(nullable = false)
  private byte id = 1;

  @Column(name = "sending_path_id", length = 36)
  private String sendingPathId;

  protected NoticeSendingPathEntity() {}

  public NoticeSendingPathEntity(String sendingPathId) {
    this.id = 1;
    this.sendingPathId = sendingPathId;
  }

  public String getSendingPathId() {
    return sendingPathId;
  }

  public void setSendingPathId(String sendingPathId) {
    this.sendingPathId = sendingPathId;
  }
}
