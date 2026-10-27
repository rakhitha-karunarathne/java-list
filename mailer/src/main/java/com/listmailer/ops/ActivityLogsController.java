package com.listmailer.ops;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogsController {

  private final ActivityLogRepository logs;

  public ActivityLogsController(ActivityLogRepository logs) {
    this.logs = logs;
  }

  @GetMapping
  public List<ActivityLogView> index(@RequestParam String kind) {
    String k = kind == null ? "" : kind.trim();
    if (!ActivityLogService.KINDS.contains(k)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown log");
    }
    return logs.findTop500ByKindOrderByCreatedAtDesc(k).stream().map(ActivityLogView::from).toList();
  }

  public record ActivityLogView(String id, String kind, String event, String message, String createdAt) {
    static ActivityLogView from(ActivityLogEntity e) {
      return new ActivityLogView(
          e.getId(), e.getKind(), e.getEvent(), e.getMessage(), e.getCreatedAt().toString());
    }
  }
}
