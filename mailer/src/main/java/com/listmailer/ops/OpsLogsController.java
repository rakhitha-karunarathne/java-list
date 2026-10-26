package com.listmailer.ops;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ops-logs")
public class OpsLogsController {

  private final OpsLogRepository opsLogs;

  public OpsLogsController(OpsLogRepository opsLogs) {
    this.opsLogs = opsLogs;
  }

  @GetMapping
  public List<OpsLogView> index(@RequestParam(required = false) String category) {
    List<OpsLogEntity> rows =
        category == null || category.isBlank()
            ? opsLogs.findAllByOrderByCreatedAtDesc()
            : opsLogs.findByCategoryOrderByCreatedAtDesc(category.trim());
    return rows.stream().map(OpsLogView::from).toList();
  }

  @GetMapping("/{id}")
  public OpsLogView detail(@PathVariable String id) {
    return opsLogs
        .findById(id)
        .map(OpsLogView::from)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public record OpsLogView(
      String id, String category, String severity, String message, String createdAt) {
    static OpsLogView from(OpsLogEntity e) {
      return new OpsLogView(
          e.getId(), e.getCategory(), e.getSeverity(), e.getMessage(), e.getCreatedAt().toString());
    }
  }
}
