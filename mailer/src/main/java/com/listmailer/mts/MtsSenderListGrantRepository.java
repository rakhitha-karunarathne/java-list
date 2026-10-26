package com.listmailer.mts;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsSenderListGrantRepository
    extends JpaRepository<MtsSenderListGrantEntity, MtsSenderListGrantEntity.Pk> {

  List<MtsSenderListGrantEntity> findByInboxIdAndSenderEmailOrderByPositionAsc(
      String inboxId, String senderEmail);

  List<MtsSenderListGrantEntity> findByInboxIdOrderBySenderEmailAscPositionAsc(String inboxId);

  List<MtsSenderListGrantEntity> findAllByOrderByInboxIdAscSenderEmailAscPositionAsc();

  void deleteByInboxIdAndSenderEmail(String inboxId, String senderEmail);
}
