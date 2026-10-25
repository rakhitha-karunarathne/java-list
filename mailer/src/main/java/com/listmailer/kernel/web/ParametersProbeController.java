package com.listmailer.kernel.web;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Temporary authenticated probe until Parameters Admin UI (catalogs epic). */
@RestController
@RequestMapping("/api/parameters")
public class ParametersProbeController {

  private final ParametersPort parameters;

  public ParametersProbeController(ParametersPort parameters) {
    this.parameters = parameters;
  }

  @GetMapping("/public-base-url")
  public Map<String, String> publicBaseUrl() {
    return Map.of(
        "key",
        ParameterKeys.PUBLIC_BASE_URL,
        "value",
        parameters.get(ParameterKeys.PUBLIC_BASE_URL).orElse(""));
  }
}
