package com.listmailer.mts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name = "mts_sender_list_grants")
@IdClass(MtsSenderListGrantEntity.Pk.class)
public class MtsSenderListGrantEntity {

  @Column(length = 36, nullable = false, unique = true)
  private String id;

  @Id
  @Column(name = "inbox_id", length = 36, nullable = false)
  private String inboxId;

  @Id
  @Column(name = "sender_email", nullable = false, length = 320)
  private String senderEmail;

  @Id
  @Column(name = "list_id", length = 36, nullable = false)
  private String listId;

  @Column(name = "substitute_from", length = 320)
  private String substituteFrom;

  @Column(name = "footer_id", length = 36)
  private String footerId;

  @Column(nullable = false)
  private int position;

  protected MtsSenderListGrantEntity() {}

  public MtsSenderListGrantEntity(
      String id,
      String inboxId,
      String senderEmail,
      String listId,
      String substituteFrom,
      String footerId,
      int position) {
    this.id = id;
    this.inboxId = inboxId;
    this.senderEmail = senderEmail.trim().toLowerCase(Locale.ROOT);
    this.listId = listId;
    this.substituteFrom = substituteFrom;
    this.footerId = footerId;
    this.position = position;
  }

  public String getId() {
    return id;
  }

  public String getInboxId() {
    return inboxId;
  }

  public String getSenderEmail() {
    return senderEmail;
  }

  public String getListId() {
    return listId;
  }

  public String getSubstituteFrom() {
    return substituteFrom;
  }

  public String getFooterId() {
    return footerId;
  }

  public int getPosition() {
    return position;
  }

  public static final class Pk implements Serializable {
    private String inboxId;
    private String senderEmail;
    private String listId;

    public Pk() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Pk pk)) return false;
      return Objects.equals(inboxId, pk.inboxId)
          && Objects.equals(senderEmail, pk.senderEmail)
          && Objects.equals(listId, pk.listId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(inboxId, senderEmail, listId);
    }
  }
}
