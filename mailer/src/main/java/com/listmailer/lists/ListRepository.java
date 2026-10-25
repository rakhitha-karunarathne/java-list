package com.listmailer.lists;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListRepository extends JpaRepository<ListEntity, String> {

  List<ListEntity> findByIdIn(Collection<String> ids);
}
