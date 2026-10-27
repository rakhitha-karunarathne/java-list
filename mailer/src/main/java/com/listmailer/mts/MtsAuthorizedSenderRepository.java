package com.listmailer.mts;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsAuthorizedSenderRepository
    extends JpaRepository<MtsAuthorizedSenderEntity, MtsAuthorizedSenderEntity.Pk> {

  List<MtsAuthorizedSenderEntity> findByInboxId(String inboxId);

  Optional<MtsAuthorizedSenderEntity> findByInboxIdAndEmail(String inboxId, String email);

  boolean existsByInboxIdAndEmail(String inboxId, String email);

  boolean existsByEmail(String email);

  void deleteByInboxIdAndEmail(String inboxId, String email);
}
