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
@Table(name = "mts_authorized_senders")
@IdClass(MtsAuthorizedSenderEntity.Pk.class)
public class MtsAuthorizedSenderEntity {

  @Id
  @Column(name = "inbox_id", length = 36, nullable = false)
  private String inboxId;

  @Id
  @Column(nullable = false, length = 320)
  private String email;

  @Column(name = "substitute_from", length = 320)
  private String substituteFrom;

  @Column(name = "sending_path_id", length = 36)
  private String sendingPathId;

  protected MtsAuthorizedSenderEntity() {}

  public MtsAuthorizedSenderEntity(String inboxId, String email) {
    this.inboxId = inboxId;
    this.email = email.trim().toLowerCase(Locale.ROOT);
  }

  public String getInboxId() {
    return inboxId;
  }

  public String getEmail() {
    return email;
  }

  public String getSubstituteFrom() {
    return substituteFrom;
  }

  public void setSubstituteFrom(String substituteFrom) {
    this.substituteFrom = substituteFrom;
  }

  public String getSendingPathId() {
    return sendingPathId;
  }

  public void setSendingPathId(String sendingPathId) {
    this.sendingPathId = sendingPathId;
  }

  public static final class Pk implements Serializable {
    private String inboxId;
    private String email;

    public Pk() {}

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Pk pk)) return false;
      return Objects.equals(inboxId, pk.inboxId) && Objects.equals(email, pk.email);
    }

    @Override
    public int hashCode() {
      return Objects.hash(inboxId, email);
    }
  }
}
