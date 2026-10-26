package com.listmailer.catalogs;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.mailbox.MailConnectionProbe;
import com.listmailer.mailbox.MailSecurity;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/bounce-mailboxes")
public class BounceMailboxesController {

  public static final int DEFAULT_POLL_MINUTES = 15;

  private final BounceMailboxRepository mailboxes;
  private final MailConnectionProbe probe;

  public BounceMailboxesController(BounceMailboxRepository mailboxes, MailConnectionProbe probe) {
    this.mailboxes = mailboxes;
    this.probe = probe;
  }

  @PostMapping("/test-connection")
  public ConnectionTestResponse testConnection(@RequestBody ConnectionTestRequest body) {
    if (body == null
        || !StringUtils.hasText(body.host())
        || body.port() <= 0
        || !StringUtils.hasText(body.username())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "host, port, and username are required");
    }
    String password = body.password();
    if (!StringUtils.hasText(password) && StringUtils.hasText(body.mailboxId())) {
      password = require(body.mailboxId()).getPassword();
    }
    MailConnectionProbe.Result result =
        probe.testImap(
            body.host(), body.port(), body.username(), password, body.useSsl(), body.useTls());
    return new ConnectionTestResponse(result.ok(), result.message());
  }

  @GetMapping
  public List<MailboxSummary> list() {
    return mailboxes.findAll().stream().map(MailboxSummary::from).toList();
  }

  @PostMapping
  public MailboxDetail create(@RequestBody MailboxRequest body) {
    validate(body, true);
    int poll = body.pollIntervalMinutes() > 0 ? body.pollIntervalMinutes() : DEFAULT_POLL_MINUTES;
    try {
      BounceMailboxEntity box =
          new BounceMailboxEntity(
              Guids.newGuid(),
              body.name().trim(),
              body.host().trim(),
              body.port(),
              body.username(),
              body.password(),
              body.useSsl(),
              poll,
              Instant.now());
      box.setUseTls(body.useTls());
      return MailboxDetail.from(mailboxes.save(box));
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @GetMapping("/{id}")
  public MailboxDetail get(@PathVariable String id) {
    return MailboxDetail.from(require(id));
  }

  @PutMapping("/{id}")
  public MailboxDetail update(@PathVariable String id, @RequestBody MailboxRequest body) {
    BounceMailboxEntity box = require(id);
    validate(body, body.password() != null);
    box.setName(body.name().trim());
    box.setHost(body.host().trim());
    box.setPort(body.port());
    box.setUsername(body.username());
    if (StringUtils.hasText(body.password())) {
      box.setPassword(body.password());
    }
    box.setUseSsl(body.useSsl());
    box.setUseTls(body.useTls());
    box.setPollIntervalMinutes(
        body.pollIntervalMinutes() > 0 ? body.pollIntervalMinutes() : DEFAULT_POLL_MINUTES);
    try {
      return MailboxDetail.from(mailboxes.save(box));
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id) {
    require(id);
    mailboxes.deleteById(id);
  }

  private BounceMailboxEntity require(String id) {
    return mailboxes
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
  }

  private static void validate(MailboxRequest body, boolean passwordRequired) {
    if (body == null
        || !StringUtils.hasText(body.name())
        || !StringUtils.hasText(body.host())
        || body.port() <= 0
        || !StringUtils.hasText(body.username())
        || (passwordRequired && !StringUtils.hasText(body.password()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid bounce mailbox");
    }
    security(body.useSsl(), body.useTls());
  }

  private static void security(boolean useSsl, boolean useTls) {
    try {
      MailSecurity.check(useSsl, useTls);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  public record MailboxRequest(
      String name,
      String host,
      int port,
      String username,
      String password,
      boolean useSsl,
      boolean useTls,
      int pollIntervalMinutes) {}

  public record ConnectionTestRequest(
      String host,
      int port,
      String username,
      String password,
      boolean useSsl,
      boolean useTls,
      String mailboxId) {}

  public record ConnectionTestResponse(boolean ok, String message) {}

  public record MailboxSummary(String id, String name, String host, int pollIntervalMinutes) {
    static MailboxSummary from(BounceMailboxEntity b) {
      return new MailboxSummary(b.getId(), b.getName(), b.getHost(), b.getPollIntervalMinutes());
    }
  }

  public record MailboxDetail(
      String id,
      String name,
      String host,
      int port,
      String username,
      boolean useSsl,
      boolean useTls,
      int pollIntervalMinutes) {
    static MailboxDetail from(BounceMailboxEntity b) {
      return new MailboxDetail(
          b.getId(),
          b.getName(),
          b.getHost(),
          b.getPort(),
          b.getUsername(),
          b.isUseSsl(),
          b.isUseTls(),
          b.getPollIntervalMinutes());
    }
  }
}
