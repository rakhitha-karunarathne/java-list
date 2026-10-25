package com.listmailer.kernel.web;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Temporary Admin Parameters API until catalogs epic Admin UI. */
@RestController
@RequestMapping("/api/parameters")
public class ParametersProbeController {

  private static final Set<String> WRITABLE =
      Set.of(
          ParameterKeys.PUBLIC_BASE_URL,
          ParameterKeys.MFA_MANDATORY,
          ParameterKeys.VERP_ENABLED,
          ParameterKeys.OPS_LOG_RETENTION_DAYS,
          ParameterKeys.AUDIT_RETENTION_DAYS);

  private final ParametersPort parameters;

  public ParametersProbeController(ParametersPort parameters) {
    this.parameters = parameters;
  }

  @GetMapping("/public-base-url")
  public Map<String, String> publicBaseUrl() {
    return get(ParameterKeys.PUBLIC_BASE_URL);
  }

  @GetMapping("/{key}")
  public Map<String, String> get(@PathVariable String key) {
    requireKnown(key);
    return Map.of("key", key, "value", parameters.get(key).orElse(""));
  }

  @PutMapping("/{key}")
  public Map<String, String> put(@PathVariable String key, @RequestBody ValueBody body) {
    requireKnown(key);
    if (body == null || body.value() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value required");
    }
    if (ParameterKeys.PUBLIC_BASE_URL.equals(key) && !body.value().startsWith("https://")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "public base URL must be https://");
    }
    if (ParameterKeys.MFA_MANDATORY.equals(key)
        && !"true".equalsIgnoreCase(body.value())
        && !"false".equalsIgnoreCase(body.value())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value must be true or false");
    }
    parameters.set(key, body.value());
    return get(key);
  }

  private void requireKnown(String key) {
    if (!WRITABLE.contains(key)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown parameter");
    }
  }

  public record ValueBody(String value) {}
}
