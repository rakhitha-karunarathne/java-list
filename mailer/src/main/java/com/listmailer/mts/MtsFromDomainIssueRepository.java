package com.listmailer.mts;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MtsFromDomainIssueRepository extends JpaRepository<MtsFromDomainIssueEntity, String> {

  List<MtsFromDomainIssueEntity> findByStatus(String status);
}
