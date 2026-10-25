package com.listmailer.catalogs;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SendingPathRepository extends JpaRepository<SendingPathEntity, String> {

  Optional<SendingPathEntity> findByNameIgnoreCase(String name);
}
