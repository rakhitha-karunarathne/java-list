package com.listmailer.bounce;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "bounce_events")
public class BounceEventEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(name = "mailbox_id", length = 36, nullable = false)
  private String mailboxId;

  @Column(name = "received_at", nullable = false)
  private Instant receivedAt;

  @Column(name = "message_id", length = 255)
  private String messageId;

  @Column(length = 998)
  private String subject;

  @Column(name = "from_addr", length = 320)
  private String fromAddr;

  @Lob
  @Column(name = "headers_json", nullable = false, columnDefinition = "MEDIUMTEXT")
  private String headersJson;

  @Lob
  @Column(name = "raw_bytes", nullable = false, columnDefinition = "LONGBLOB")
  private byte[] rawBytes;

  /** INGESTED | CORRELATED | UNCORRELATED | PROCESSED */
  @Column(nullable = false, length = 32)
  private String status;

  protected BounceEventEntity() {}

  public BounceEventEntity(
      String id,
      String mailboxId,
      Instant receivedAt,
      String messageId,
      String subject,
      String fromAddr,
      String headersJson,
      byte[] rawBytes,
      String status) {
    this.id = id;
    this.mailboxId = mailboxId;
    this.receivedAt = receivedAt;
    this.messageId = messageId;
    this.subject = subject;
    this.fromAddr = fromAddr;
    this.headersJson = headersJson;
    this.rawBytes = rawBytes;
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public String getMailboxId() {
    return mailboxId;
  }

  public Instant getReceivedAt() {
    return receivedAt;
  }

  public String getMessageId() {
    return messageId;
  }

  public String getSubject() {
    return subject;
  }

  public String getFromAddr() {
    return fromAddr;
  }

  public String getHeadersJson() {
    return headersJson;
  }

  public byte[] getRawBytes() {
    return rawBytes;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
