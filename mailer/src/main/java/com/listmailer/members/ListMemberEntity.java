package com.listmailer.members;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "list_members")
@IdClass(ListMemberEntity.Pk.class)
public class ListMemberEntity {

  @Id
  @Column(name = "list_id", length = 36, nullable = false)
  private String listId;

  @Id
  @Column(name = "member_id", length = 36, nullable = false)
  private String memberId;

  @Column(nullable = false)
  private boolean unsubscribed;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ListMemberEntity() {}

  public ListMemberEntity(String listId, String memberId, boolean unsubscribed, Instant createdAt) {
    this.listId = listId;
    this.memberId = memberId;
    this.unsubscribed = unsubscribed;
    this.createdAt = createdAt;
  }

  public String getListId() {
    return listId;
  }

  public String getMemberId() {
    return memberId;
  }

  public boolean isUnsubscribed() {
    return unsubscribed;
  }

  public void setUnsubscribed(boolean unsubscribed) {
    this.unsubscribed = unsubscribed;
  }

  public static final class Pk implements Serializable {
    private String listId;
    private String memberId;

    public Pk() {}

    public String getListId() {
      return listId;
    }

    public void setListId(String listId) {
      this.listId = listId;
    }

    public String getMemberId() {
      return memberId;
    }

    public void setMemberId(String memberId) {
      this.memberId = memberId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Pk pk)) {
        return false;
      }
      return Objects.equals(listId, pk.listId) && Objects.equals(memberId, pk.memberId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(listId, memberId);
    }
  }
}
