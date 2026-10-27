package com.listmailer.ops;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogsController {

  private final ActivityLogRepository logs;
  private final ParametersPort parameters;

  public ActivityLogsController(ActivityLogRepository logs, ParametersPort parameters) {
    this.logs = logs;
    this.parameters = parameters;
  }

  @GetMapping
  public List<ActivityLogView> index(
      @RequestParam String kind, @RequestParam(required = false) String mailboxId) {
    String k = kind == null ? "" : kind.trim();
    if (!ActivityLogService.KINDS.contains(k)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown log");
    }
    if (StringUtils.hasText(mailboxId)) {
      return logs.findByKindAndMailboxId(k, mailboxId.trim()).stream()
          .limit(500)
          .map(ActivityLogView::from)
          .toList();
    }
    return logs.findTop500ByKindOrderByCreatedAtDesc(k).stream().map(ActivityLogView::from).toList();
  }

  @GetMapping("/enabled")
  public EnabledView enabled(@RequestParam String kind) {
    String k = kind == null ? "" : kind.trim();
    if (!ActivityLogService.KINDS.contains(k)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown log");
    }
    String key =
        switch (k) {
          case ActivityLogService.BOUNCE -> ParameterKeys.BOUNCE_PROCESS_LOG_ENABLED;
          case ActivityLogService.SEND -> ParameterKeys.SEND_QUEUE_LOG_ENABLED;
          case ActivityLogService.MTS -> ParameterKeys.MTS_PROCESS_LOG_ENABLED;
          default -> null;
        };
    boolean on =
        key == null
            || parameters.get(key).map(v -> !"false".equalsIgnoreCase(v.trim())).orElse(true);
    return new EnabledView(on);
  }

  public record EnabledView(boolean enabled) {}

  public record ActivityLogView(String id, String kind, String event, String message, String createdAt) {
    static ActivityLogView from(ActivityLogEntity e) {
      return new ActivityLogView(
          e.getId(), e.getKind(), e.getEvent(), e.getMessage(), e.getCreatedAt().toString());
    }
  }
}
