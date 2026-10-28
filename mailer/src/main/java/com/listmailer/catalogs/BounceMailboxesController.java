package com.listmailer.catalogs;

import com.listmailer.bounce.BouncePollService;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.mailbox.MailConnectionProbe;
import com.listmailer.mailbox.MailboxPollLock;
import com.listmailer.mailbox.MailSecurity;
import com.listmailer.secrets.SecretStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
  private final BouncePollService poll;
  private final MailboxPollLock locks;
  private final SecretStore secrets;

  public BounceMailboxesController(
      BounceMailboxRepository mailboxes,
      MailConnectionProbe probe,
      BouncePollService poll,
      MailboxPollLock locks,
      SecretStore secrets) {
    this.mailboxes = mailboxes;
    this.probe = probe;
    this.poll = poll;
    this.locks = locks;
    this.secrets = secrets;
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
      password = secrets.requirePlain(require(body.mailboxId()).getPasswordSecretId());
    }
    MailConnectionProbe.Result result =
        probe.testImap(
            body.host(), body.port(), body.username(), password, body.useSsl(), body.useTls());
    return new ConnectionTestResponse(result.ok(), result.message());
  }

  @GetMapping
  public List<MailboxSummary> list() {
    return mailboxes.findAll().stream().map(b -> MailboxSummary.from(b, locks)).toList();
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
              secrets.create(body.password()),
              body.useSsl(),
              poll,
              Instant.now());
      box.setUseTls(body.useTls());
      return MailboxDetail.from(mailboxes.save(box), locks);
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @GetMapping("/{id}")
  public MailboxDetail get(@PathVariable String id) {
    return MailboxDetail.from(require(id), locks);
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
      secrets.update(box.getPasswordSecretId(), body.password());
    }
    box.setUseSsl(body.useSsl());
    box.setUseTls(body.useTls());
    box.setPollIntervalMinutes(
        body.pollIntervalMinutes() > 0 ? body.pollIntervalMinutes() : DEFAULT_POLL_MINUTES);
    try {
      return MailboxDetail.from(mailboxes.save(box), locks);
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "name already exists");
    }
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id) {
    String secretId = require(id).getPasswordSecretId();
    mailboxes.deleteById(id);
    secrets.delete(secretId);
  }

  @PostMapping("/{id}/poll")
  public Map<String, String> pollNow(@PathVariable String id) {
    BounceMailboxEntity box = require(id);
    if (!box.isEnabled()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "mailbox is disabled");
    }
    poll.startPollAsync(id);
    return Map.of("message", "process started");
  }

  @PostMapping("/{id}/break-lock")
  public MailboxDetail breakLock(@PathVariable String id) {
    require(id);
    poll.breakLock(id);
    return MailboxDetail.from(require(id), locks);
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

  public record MailboxSummary(
      String id,
      String name,
      int pollIntervalMinutes,
      String lastPolledAt,
      String result,
      boolean enabled) {
    static MailboxSummary from(BounceMailboxEntity b, MailboxPollLock locks) {
      return new MailboxSummary(
          b.getId(),
          b.getName(),
          b.getPollIntervalMinutes(),
          b.getLastPolledAt() == null ? null : b.getLastPolledAt().toString(),
          pollResult(b),
          b.isEnabled());
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
      int pollIntervalMinutes,
      boolean enabled,
      String lastPolledAt,
      String lastPollingUpdate,
      String result,
      boolean polling) {
    static MailboxDetail from(BounceMailboxEntity b, MailboxPollLock locks) {
      Instant now = Instant.now();
      boolean held = locks.stillHeld(b.isPolling(), b.getLastPollingUpdate(), now);
      return new MailboxDetail(
          b.getId(),
          b.getName(),
          b.getHost(),
          b.getPort(),
          b.getUsername(),
          b.isUseSsl(),
          b.isUseTls(),
          b.getPollIntervalMinutes(),
          b.isEnabled(),
          b.getLastPolledAt() == null ? null : b.getLastPolledAt().toString(),
          b.getLastPollingUpdate() == null ? null : b.getLastPollingUpdate().toString(),
          pollResult(b),
          held);
    }
  }

  static String pollResult(BounceMailboxEntity b) {
    if (b.getLastPollOk() == null) {
      return "";
    }
    return b.getLastPollOk() ? "OK" : "Errors";
  }
}
