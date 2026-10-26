package com.listmailer.mts;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsBlockRepository extends JpaRepository<MtsBlockEntity, String> {

  boolean existsByKindAndValue(String kind, String value);

  Optional<MtsBlockEntity> findByKindAndValue(String kind, String value);
}
