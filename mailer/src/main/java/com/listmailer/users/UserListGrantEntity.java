package com.listmailer.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_list_grants")
@IdClass(UserListGrantEntity.Pk.class)
public class UserListGrantEntity {

  @Id
  @Column(name = "user_id", length = 36, nullable = false)
  private String userId;

  @Id
  @Column(name = "list_id", length = 36, nullable = false)
  private String listId;

  @Column(name = "can_manage", nullable = false)
  private boolean canManage;

  @Column(name = "can_send", nullable = false)
  private boolean canSend;

  protected UserListGrantEntity() {}

  public UserListGrantEntity(String userId, String listId, boolean canManage, boolean canSend) {
    this.userId = userId;
    this.listId = listId;
    this.canManage = canManage;
    this.canSend = canSend;
  }

  public String getUserId() {
    return userId;
  }

  public String getListId() {
    return listId;
  }

  public boolean isCanManage() {
    return canManage;
  }

  public boolean isCanSend() {
    return canSend;
  }

  public void setCanManage(boolean canManage) {
    this.canManage = canManage;
  }

  public void setCanSend(boolean canSend) {
    this.canSend = canSend;
  }

  public static final class Pk implements Serializable {
    private String userId;
    private String listId;

    public Pk() {}

    public Pk(String userId, String listId) {
      this.userId = userId;
      this.listId = listId;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
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
      return Objects.equals(userId, pk.userId) && Objects.equals(listId, pk.listId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, listId);
    }
  }
}
