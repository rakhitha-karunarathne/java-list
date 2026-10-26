package com.listmailer.catalogs;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.mailbox.MailConnectionProbe;
import com.listmailer.mailbox.MailSecurity;
import com.listmailer.mts.MtsFromDomainService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sending-paths")
public class SendingPathsController {

  private final SendingPathRepository paths;
  private final CatalogReferenceService references;
  private final MtsFromDomainService fromDomain;
  private final MailConnectionProbe probe;

  public SendingPathsController(
      SendingPathRepository paths,
      CatalogReferenceService references,
      MtsFromDomainService fromDomain,
      MailConnectionProbe probe) {
    this.paths = paths;
    this.references = references;
    this.fromDomain = fromDomain;
    this.probe = probe;
  }

  @PostMapping("/test-connection")
  public ConnectionTestResponse testConnection(@RequestBody ConnectionTestRequest body) {
    if (body == null || !StringUtils.hasText(body.host()) || body.port() <= 0 || !StringUtils.hasText(body.username())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "host, port, and username are required");
    }
    String password = body.password();
    if (!StringUtils.hasText(password) && StringUtils.hasText(body.pathId())) {
      password = require(body.pathId()).getPassword();
    }
    MailConnectionProbe.Result result =
        probe.testSmtp(
            body.host(), body.port(), body.username(), password, body.useSsl(), body.useTls());
    return new ConnectionTestResponse(result.ok(), result.message());
  }

  @GetMapping
  public List<PathSummary> list() {
    return paths.findAll().stream().map(PathSummary::from).toList();
  }

  @PostMapping
  public PathDetail create(@RequestBody PathRequest body) {
    validate(body, true);
    try {
      SendingPathEntity saved =
          paths.save(
              path(
                  new SendingPathEntity(
                      Guids.newGuid(),
                      body.name().trim(),
                      body.host().trim(),
                      body.port(),
                      body.username(),
                      body.password(),
                      body.fromEmail().trim(),
                      body.fromName().trim(),
                      body.useTls(),
                      Instant.now()),
                  body.useSsl()));
      return PathDetail.from(saved, List.of());
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @GetMapping("/{id}")
  public PathDetail get(@PathVariable String id) {
    return PathDetail.from(require(id), usedBy(id));
  }

  @PutMapping("/{id}")
  public PathDetail update(@PathVariable String id, @RequestBody PathRequest body) {
    SendingPathEntity path = require(id);
    validate(body, body.password() != null);
    path.setName(body.name().trim());
    path.setHost(body.host().trim());
    path.setPort(body.port());
    path.setUsername(body.username());
    if (StringUtils.hasText(body.password())) {
      path.setPassword(body.password());
    }
    path.setFromEmail(body.fromEmail().trim());
    path.setFromName(body.fromName().trim());
    path.setUseTls(body.useTls());
    path.setUseSsl(body.useSsl());
    try {
      SendingPathEntity saved = paths.save(path);
      fromDomain.revalidateForSendingPath(id);
      return PathDetail.from(saved, usedBy(id));
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @DeleteMapping("/{id}")
  @Transactional
  public ResponseEntity<?> delete(@PathVariable String id) {
    require(id);
    List<UsedBy> used = usedBy(id);
    if (!used.isEmpty() || references.isReferenced(CatalogTargetType.SENDING_PATH, id)) {
      used = usedBy(id);
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("code", "IN_USE");
      body.put("message", "Sending path is referenced");
      body.put("usedBy", used);
      return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    paths.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private SendingPathEntity require(String id) {
    return paths
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
  }

  private List<UsedBy> usedBy(String id) {
    return references.usedBy(CatalogTargetType.SENDING_PATH, id).stream()
        .map(r -> new UsedBy(r.getSourceType(), r.getSourceId(), r.getLabel()))
        .toList();
  }

  private static void validate(PathRequest body, boolean passwordRequired) {
    if (body == null
        || !StringUtils.hasText(body.name())
        || !StringUtils.hasText(body.host())
        || body.port() <= 0
        || !StringUtils.hasText(body.username())
        || !StringUtils.hasText(body.fromEmail())
        || !StringUtils.hasText(body.fromName())
        || (passwordRequired && !StringUtils.hasText(body.password()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid sending path");
    }
    security(body.useSsl(), body.useTls());
  }

  private static SendingPathEntity path(SendingPathEntity entity, boolean useSsl) {
    entity.setUseSsl(useSsl);
    return entity;
  }

  private static void security(boolean useSsl, boolean useTls) {
    try {
      MailSecurity.check(useSsl, useTls);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  public record PathRequest(
      String name,
      String host,
      int port,
      String username,
      String password,
      String fromEmail,
      String fromName,
      boolean useTls,
      boolean useSsl) {}

  public record ConnectionTestRequest(
      String host,
      int port,
      String username,
      String password,
      boolean useTls,
      boolean useSsl,
      String pathId) {}

  public record ConnectionTestResponse(boolean ok, String message) {}

  public record PathSummary(String id, String name, String host, int port, String fromEmail) {
    static PathSummary from(SendingPathEntity p) {
      return new PathSummary(p.getId(), p.getName(), p.getHost(), p.getPort(), p.getFromEmail());
    }
  }

  public record UsedBy(String sourceType, String sourceId, String label) {}

  public record PathDetail(
      String id,
      String name,
      String host,
      int port,
      String username,
      String fromEmail,
      String fromName,
      boolean useTls,
      boolean useSsl,
      List<UsedBy> usedBy) {
    static PathDetail from(SendingPathEntity p, List<UsedBy> usedBy) {
      return new PathDetail(
          p.getId(),
          p.getName(),
          p.getHost(),
          p.getPort(),
          p.getUsername(),
          p.getFromEmail(),
          p.getFromName(),
          p.isUseTls(),
          p.isUseSsl(),
          usedBy);
    }
  }
}
