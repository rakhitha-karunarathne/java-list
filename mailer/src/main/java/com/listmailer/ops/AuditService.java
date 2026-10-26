package com.listmailer.ops;

import com.listmailer.users.ListAccessService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuditService {

  private final AuditLogRepository audit;
  private final ListAccessService access;

  public AuditService(AuditLogRepository audit, ListAccessService access) {
    this.audit = audit;
    this.access = access;
  }

  public List<AuditLogEntity> forCurrentUser() {
    if (access.isListSenderOnly()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Audit not available");
    }
    if (access.isAdmin()) {
      return audit.findAllByOrderByCreatedAtDesc();
    }
    if (access.isListManager()) {
      List<String> lists = access.managedListIds();
      if (lists.isEmpty()) {
        return List.of();
      }
      return audit.findByListIdInOrderByCreatedAtDesc(lists);
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Audit not available");
  }
}
