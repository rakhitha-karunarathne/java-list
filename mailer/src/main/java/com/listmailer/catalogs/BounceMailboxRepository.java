package com.listmailer.catalogs;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BounceMailboxRepository extends JpaRepository<BounceMailboxEntity, String> {}
