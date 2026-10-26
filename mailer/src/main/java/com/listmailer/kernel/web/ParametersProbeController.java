package com.listmailer.kernel.web;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.parameters.ParameterDefinitions;
import com.listmailer.parameters.ParameterEntity;
import com.listmailer.parameters.ParameterRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Admin Parameters API (descriptions + values). */
@RestController
@RequestMapping("/api/parameters")
public class ParametersProbeController {

  private final ParametersPort parameters;
  private final ParameterRepository repository;

  public ParametersProbeController(ParametersPort parameters, ParameterRepository repository) {
    this.parameters = parameters;
    this.repository = repository;
  }

  @GetMapping
  public List<ParameterView> list() {
    return ParameterDefinitions.all().values().stream()
        .map(
            def ->
                new ParameterView(
                    def.key(),
                    parameters.get(def.key()).orElse(def.defaultValue()),
                    def.description()))
        .toList();
  }

  @GetMapping("/public-base-url")
  public Map<String, String> publicBaseUrl() {
    ParameterView view = get(ParameterKeys.PUBLIC_BASE_URL);
    return Map.of("key", view.key(), "value", view.value());
  }

  @GetMapping("/{key}")
  public ParameterView get(@PathVariable String key) {
    ParameterDefinitions.Def def;
    try {
      def = ParameterDefinitions.require(key);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown parameter");
    }
    String description =
        repository.findById(key).map(ParameterEntity::getDescription).orElse(def.description());
    return new ParameterView(key, parameters.get(key).orElse(def.defaultValue()), description);
  }

  @PutMapping("/{key}")
  public ParameterView put(@PathVariable String key, @RequestBody ValueBody body) {
    ParameterDefinitions.Def def;
    try {
      def = ParameterDefinitions.require(key);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown parameter");
    }
    if (body == null || body.value() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value required");
    }
    if (ParameterKeys.PUBLIC_BASE_URL.equals(key) && !body.value().startsWith("https://")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "public base URL must be https://");
    }
    if ((ParameterKeys.MFA_MANDATORY.equals(key)
            || ParameterKeys.VERP_ENABLED.equals(key)
            || ParameterKeys.BOUNCE_PROCESS_LOG_ENABLED.equals(key)
            || ParameterKeys.SEND_QUEUE_LOG_ENABLED.equals(key)
            || ParameterKeys.MTS_PROCESS_LOG_ENABLED.equals(key))
        && !"true".equalsIgnoreCase(body.value())
        && !"false".equalsIgnoreCase(body.value())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value must be true or false");
    }
    if (ParameterKeys.OPS_LOG_RETENTION_DAYS.equals(key)
        || ParameterKeys.AUDIT_RETENTION_DAYS.equals(key)
        || ParameterKeys.BOUNCE_PROCESS_LOG_RETENTION_DAYS.equals(key)
        || ParameterKeys.SEND_QUEUE_LOG_RETENTION_DAYS.equals(key)
        || ParameterKeys.MTS_PROCESS_LOG_RETENTION_DAYS.equals(key)
        || ParameterKeys.SEND_BATCH_SIZE.equals(key)
        || ParameterKeys.SEND_BATCH_DURATION_MINUTES.equals(key)
        || ParameterKeys.SEND_MESSAGES_PER_HOUR.equals(key)) {
      requireInt(body.value(), 1);
    }
    if (ParameterKeys.SEND_DELAY_PER_EMAIL_MS.equals(key)) {
      requireInt(body.value(), 0);
    }
    if (body.value().length() > 2000) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "value too long");
    }
    parameters.set(key, body.value());
    return new ParameterView(key, body.value(), def.description());
  }

  private static void requireInt(String value, int min) {
    try {
      if (Integer.parseInt(value.trim()) < min) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "value must be an integer >= " + min);
    }
  }

  public record ValueBody(String value) {}

  public record ParameterView(String key, String value, String description) {}
}
