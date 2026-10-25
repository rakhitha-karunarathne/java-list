package com.listmailer.users;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBackupCodeRepository extends JpaRepository<UserBackupCodeEntity, String> {

  List<UserBackupCodeEntity> findByUserIdAndUsedAtIsNull(String userId);

  void deleteByUserId(String userId);

  @Modifying
  @Query(
      "update UserBackupCodeEntity c set c.usedAt = :usedAt where c.id = :id and c.usedAt is null")
  int markUsedIfUnused(@Param("id") String id, @Param("usedAt") Instant usedAt);
}
