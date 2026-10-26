package com.listmailer.mts;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsAuthorizedSenderRepository
    extends JpaRepository<MtsAuthorizedSenderEntity, MtsAuthorizedSenderEntity.Pk> {

  List<MtsAuthorizedSenderEntity> findByInboxId(String inboxId);

  boolean existsByInboxIdAndEmail(String inboxId, String email);

  boolean existsByEmail(String email);

  void deleteByInboxIdAndEmail(String inboxId, String email);
}
