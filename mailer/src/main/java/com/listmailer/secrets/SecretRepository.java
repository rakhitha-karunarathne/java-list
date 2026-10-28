package com.listmailer.secrets;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SecretRepository extends JpaRepository<SecretEntity, String> {

  long countByKeyVersionLessThan(int version);

  long countByKeyVersionGreaterThan(int version);

  @Query("select s from SecretEntity s where s.keyVersion < :version")
  List<SecretEntity> findByKeyVersionLessThan(@Param("version") int version);
}
