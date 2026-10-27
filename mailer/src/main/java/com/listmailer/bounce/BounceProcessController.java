package com.listmailer.bounce;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/bounce-process")
public class BounceProcessController {

  private final BounceProcessService process;

  public BounceProcessController(BounceProcessService process) {
    this.process = process;
  }

  @PostMapping("/run")
  public Map<String, String> runNow() {
    return switch (process.startProcessAsync()) {
      case ALREADY_RUNNING ->
          throw new ResponseStatusException(HttpStatus.CONFLICT, "bounce process already running");
      case SKIPPED -> Map.of("message", "skipped — no new bounces since last run");
      case STARTED -> Map.of("message", "process started");
    };
  }
}
