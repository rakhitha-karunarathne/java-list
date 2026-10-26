package com.listmailer.ops;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

  private final AuditService audit;

  public AuditController(AuditService audit) {
    this.audit = audit;
  }

  @GetMapping
  public List<AuditView> index() {
    return audit.forCurrentUser().stream().map(AuditView::from).toList();
  }

  public record AuditView(
      String id,
      String action,
      String detail,
      String actor,
      String listId,
      String memberId,
      String createdAt) {
    static AuditView from(AuditLogEntity e) {
      return new AuditView(
          e.getId(),
          e.getAction(),
          e.getDetail(),
          e.getActor(),
          e.getListId(),
          e.getMemberId(),
          e.getCreatedAt().toString());
    }
  }
}
