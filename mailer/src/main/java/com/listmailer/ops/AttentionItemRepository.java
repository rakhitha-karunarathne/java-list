package com.listmailer.ops;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttentionItemRepository extends JpaRepository<AttentionItemEntity, String> {

  List<AttentionItemEntity> findByOpenTrueOrderByCreatedAtDesc();

  List<AttentionItemEntity> findByOpenTrueAndListIdInOrderByCreatedAtDesc(List<String> listIds);
}
