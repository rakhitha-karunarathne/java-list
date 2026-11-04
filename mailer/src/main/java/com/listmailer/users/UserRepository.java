package com.listmailer.users;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {

  Optional<UserEntity> findByEmailIgnoreCase(String email);

  List<UserEntity> findByRoleAndEnabledTrueAndReceiveNotificationEmailsTrue(UserRole role);

  long count();
}
