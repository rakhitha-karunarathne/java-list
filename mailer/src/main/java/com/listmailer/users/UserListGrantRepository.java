package com.listmailer.users;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserListGrantRepository
    extends JpaRepository<UserListGrantEntity, UserListGrantEntity.Pk> {

  List<UserListGrantEntity> findByUserId(String userId);

  Optional<UserListGrantEntity> findByUserIdAndListId(String userId, String listId);

  void deleteByUserIdAndListId(String userId, String listId);
}
