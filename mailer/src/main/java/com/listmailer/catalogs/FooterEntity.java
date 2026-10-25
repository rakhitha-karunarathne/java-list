package com.listmailer.catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "footers")
public class FooterEntity {

  @Id
  @Column(length = 36, nullable = false)
  private String id;

  @Column(nullable = false, length = 120, unique = true)
  private String name;

  @Lob
  @Column(name = "html_body", nullable = false, columnDefinition = "MEDIUMTEXT")
  private String htmlBody;

  @Lob
  @Column(name = "text_body", nullable = false, columnDefinition = "MEDIUMTEXT")
  private String textBody;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FooterEntity() {}

  public FooterEntity(String id, String name, String htmlBody, String textBody, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.htmlBody = htmlBody;
    this.textBody = textBody;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHtmlBody() {
    return htmlBody;
  }

  public void setHtmlBody(String htmlBody) {
    this.htmlBody = htmlBody;
  }

  public String getTextBody() {
    return textBody;
  }

  public void setTextBody(String textBody) {
    this.textBody = textBody;
  }
}
