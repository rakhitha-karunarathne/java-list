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
    if (!process.startProcessAsync()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "bounce process already running");
    }
    return Map.of("message", "process started");
  }
}
