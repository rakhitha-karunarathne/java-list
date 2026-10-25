package com.listmailer.users;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class ListAccessService {

  private final UserRepository users;
  private final UserListGrantRepository grants;

  public ListAccessService(UserRepository users, UserListGrantRepository grants) {
    this.users = users;
    this.grants = grants;
  }

  public boolean canManage(String listId) {
    return isAdmin() || grant(listId).map(UserListGrantEntity::isCanManage).orElse(false);
  }

  public boolean canSend(String listId) {
    return isAdmin() || grant(listId).map(UserListGrantEntity::isCanSend).orElse(false);
  }

  public boolean canAccess(String listId) {
    return canManage(listId) || canSend(listId);
  }

  public boolean isAdmin() {
    return currentUser().map(u -> u.getRole() == UserRole.ADMIN).orElse(false);
  }

  private java.util.Optional<UserListGrantEntity> grant(String listId) {
    return currentUserId().flatMap(uid -> grants.findByUserIdAndListId(uid, listId));
  }

  private java.util.Optional<UserEntity> currentUser() {
    return currentUserId().flatMap(users::findById);
  }

  private java.util.Optional<String> currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      return java.util.Optional.ofNullable(jwt.getSubject());
    }
    return java.util.Optional.empty();
  }
}
