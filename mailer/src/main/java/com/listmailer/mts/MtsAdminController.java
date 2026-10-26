package com.listmailer.mts;

import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.lists.ListRepository;
import com.listmailer.mailbox.MailConnectionProbe;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/mts")
public class MtsAdminController {

  public static final int DEFAULT_POLL = 15;

  private final MtsInboxRepository inboxes;
  private final MtsAuthorizedSenderRepository senders;
  private final MtsMappingRepository mappings;
  private final MtsMappingListRepository mappingLists;
  private final ListRepository lists;
  private final MtsFromDomainService fromDomain;
  private final MtsDropService drops;
  private final CatalogReferenceService references;
  private final MailConnectionProbe probe;

  public MtsAdminController(
      MtsInboxRepository inboxes,
      MtsAuthorizedSenderRepository senders,
      MtsMappingRepository mappings,
      MtsMappingListRepository mappingLists,
      ListRepository lists,
      MtsFromDomainService fromDomain,
      MtsDropService drops,
      CatalogReferenceService references,
      MailConnectionProbe probe) {
    this.inboxes = inboxes;
    this.senders = senders;
    this.mappings = mappings;
    this.mappingLists = mappingLists;
    this.lists = lists;
    this.fromDomain = fromDomain;
    this.drops = drops;
    this.references = references;
    this.probe = probe;
  }

  @GetMapping("/drop-log")
  public List<DropView> dropLog(@org.springframework.web.bind.annotation.RequestParam(required = false) String inboxId) {
    return drops.list(inboxId).stream()
        .map(
            d ->
                new DropView(
                    d.getId(),
                    d.getInboxId(),
                    d.getSenderEmail(),
                    d.getReason(),
                    d.isCountedForAutoBlock(),
                    d.getCreatedAt().toString(),
                    d.isResolved()))
        .toList();
  }

  @GetMapping("/blocks")
  public List<BlockView> blocks() {
    return drops.allBlocks().stream()
        .map(b -> new BlockView(b.getId(), b.getKind(), b.getValue(), b.getSource()))
        .toList();
  }

  @PostMapping("/blocks/sender")
  public Map<String, String> blockSender(@RequestBody SenderBody body) {
    drops.blockSender(body.email(), "ADMIN");
    return Map.of("status", "blocked");
  }

  @PostMapping("/blocks/domain")
  public Map<String, String> blockDomain(@RequestBody Map<String, String> body) {
    drops.blockDomain(body.get("domain"), "ADMIN");
    return Map.of("status", "blocked");
  }

  @DeleteMapping("/blocks/{id}")
  public Map<String, String> unblock(@PathVariable String id) {
    drops.unblock(id);
    return Map.of("status", "unblocked");
  }

  @PostMapping("/drop-log/{id}/resolve")
  public Map<String, String> resolveDrop(@PathVariable String id) {
    drops.resolve(id);
    return Map.of("status", "resolved");
  }

  @GetMapping("/from-domain-issues")
  public List<IssueView> issues() {
    return fromDomain.openIssues().stream()
        .map(
            i ->
                new IssueView(
                    i.getId(),
                    i.getInboxId(),
                    i.getMappingId(),
                    i.getSenderEmail(),
                    i.getSubject(),
                    i.getStatus(),
                    i.getResolution(),
                    i.getDetail(),
                    i.getCreatedAt().toString()))
        .toList();
  }

  @PostMapping("/from-domain-issues/{id}/clear")
  public Map<String, String> clearIssue(@PathVariable String id) {
    fromDomain.clear(id);
    return Map.of("status", "CLEARED");
  }

  @GetMapping("/inboxes")
  public List<InboxSummary> index() {
    return inboxes.findAll().stream()
        .map(i -> new InboxSummary(i.getId(), i.getName(), i.isEnabled(), i.getPollIntervalMinutes()))
        .toList();
  }

  @PostMapping("/inboxes/test-connection")
  public ConnectionTestResponse testInboxConnection(@RequestBody ConnectionTestRequest body) {
    if (body == null || !StringUtils.hasText(body.host()) || body.port() <= 0 || !StringUtils.hasText(body.username())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "host, port, and username are required");
    }
    String password = body.password();
    if (!StringUtils.hasText(password) && StringUtils.hasText(body.inboxId())) {
      password = requireInbox(body.inboxId()).getPassword();
    }
    MailConnectionProbe.Result result =
        probe.testImap(body.host(), body.port(), body.username(), password, body.useSsl());
    return new ConnectionTestResponse(result.ok(), result.message());
  }

  @PostMapping("/inboxes")
  public InboxDetail createInbox(@RequestBody InboxBody body) {
    validateInbox(body);
    int poll = body.pollIntervalMinutes() > 0 ? body.pollIntervalMinutes() : DEFAULT_POLL;
    MtsInboxEntity saved =
        inboxes.save(
            new MtsInboxEntity(
                Guids.newGuid(),
                body.name().trim(),
                body.host().trim(),
                body.port(),
                body.username(),
                body.password(),
                body.useSsl(),
                poll,
                Instant.now()));
    return detail(saved);
  }

  @GetMapping("/inboxes/{id}")
  public InboxDetail getInbox(@PathVariable String id) {
    return detail(requireInbox(id));
  }

  @PutMapping("/inboxes/{id}")
  public InboxDetail updateInbox(@PathVariable String id, @RequestBody InboxBody body) {
    validateInbox(body);
    MtsInboxEntity box = requireInbox(id);
    box.setName(body.name().trim());
    box.setHost(body.host().trim());
    box.setPort(body.port());
    box.setUsername(body.username());
    if (StringUtils.hasText(body.password())) {
      box.setPassword(body.password());
    }
    box.setUseSsl(body.useSsl());
    if (body.pollIntervalMinutes() > 0) {
      box.setPollIntervalMinutes(body.pollIntervalMinutes());
    }
    if (body.enabled() != null) {
      box.setEnabled(body.enabled());
    }
    return detail(inboxes.save(box));
  }

  @PostMapping("/inboxes/{id}/senders")
  @Transactional
  public InboxDetail addSender(@PathVariable String id, @RequestBody SenderBody body) {
    requireInbox(id);
    if (body == null || !StringUtils.hasText(body.email())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email required");
    }
    String email = body.email().trim().toLowerCase(Locale.ROOT);
    if (!senders.existsByInboxIdAndEmail(id, email)) {
      senders.save(new MtsAuthorizedSenderEntity(id, email));
    }
    return detail(requireInbox(id));
  }

  @DeleteMapping("/inboxes/{id}/senders/{email}")
  @Transactional
  public InboxDetail removeSender(@PathVariable String id, @PathVariable String email) {
    requireInbox(id);
    senders.deleteByInboxIdAndEmail(id, email.trim().toLowerCase(Locale.ROOT));
    return detail(requireInbox(id));
  }

  @GetMapping("/mappings")
  public List<MappingSummary> allMappings() {
    List<MappingSummary> out = new ArrayList<>();
    for (MtsMappingEntity m : mappings.findAll()) {
      out.add(mappingSummary(m));
    }
    return out;
  }

  @PostMapping("/inboxes/{id}/mappings")
  @Transactional
  public MappingSummary createMapping(@PathVariable String id, @RequestBody MappingBody body) {
    requireInbox(id);
    if (body == null || !StringUtils.hasText(body.name()) || body.listIds() == null || body.listIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and listIds required");
    }
    for (String listId : body.listIds()) {
      if (!lists.existsById(listId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown list");
      }
    }
    MtsMappingEntity m =
        mappings.save(
            new MtsMappingEntity(
                Guids.newGuid(),
                id,
                body.name().trim(),
                blankToNull(body.substituteFrom()),
                blankToNull(body.footerId()),
                Instant.now()));
    int i = 0;
    for (String listId : body.listIds()) {
      mappingLists.save(new MtsMappingListEntity(m.getId(), listId, i++));
      lists
          .findById(listId)
          .ifPresent(
              list ->
                  references.add(
                      CatalogTargetType.SENDING_PATH,
                      list.getSendingPathId(),
                      "MTS_MAPPING",
                      m.getId(),
                      "MTS mapping " + m.getName()));
    }
    if (StringUtils.hasText(m.getFooterId())) {
      references.add(
          CatalogTargetType.FOOTER,
          m.getFooterId(),
          "MTS_MAPPING",
          m.getId(),
          "MTS mapping " + m.getName());
    }
    return mappingSummary(m);
  }

  @DeleteMapping("/mappings/{id}")
  @Transactional
  public Map<String, String> deleteMapping(@PathVariable String id) {
    MtsMappingEntity m =
        mappings
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    for (MtsMappingListEntity ml : mappingLists.findByMappingIdOrderByPositionAsc(id)) {
      lists
          .findById(ml.getListId())
          .ifPresent(
              list ->
                  references.remove(
                      CatalogTargetType.SENDING_PATH,
                      list.getSendingPathId(),
                      "MTS_MAPPING",
                      id));
    }
    if (StringUtils.hasText(m.getFooterId())) {
      references.remove(CatalogTargetType.FOOTER, m.getFooterId(), "MTS_MAPPING", id);
    }
    mappingLists.deleteByMappingId(id);
    mappings.delete(m);
    return Map.of("status", "deleted");
  }

  private InboxDetail detail(MtsInboxEntity box) {
    List<String> emails =
        senders.findByInboxId(box.getId()).stream().map(MtsAuthorizedSenderEntity::getEmail).toList();
    List<MappingSummary> maps =
        mappings.findByInboxId(box.getId()).stream().map(this::mappingSummary).toList();
    return new InboxDetail(
        box.getId(),
        box.getName(),
        box.getHost(),
        box.getPort(),
        box.getUsername(),
        box.isUseSsl(),
        box.getPollIntervalMinutes(),
        box.isEnabled(),
        emails,
        maps);
  }

  private MappingSummary mappingSummary(MtsMappingEntity m) {
    List<String> listIds =
        mappingLists.findByMappingIdOrderByPositionAsc(m.getId()).stream()
            .map(MtsMappingListEntity::getListId)
            .toList();
    return new MappingSummary(
        m.getId(), m.getInboxId(), m.getName(), m.getSubstituteFrom(), m.getFooterId(), listIds);
  }

  private MtsInboxEntity requireInbox(String id) {
    return inboxes
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "inbox not found"));
  }

  private static void validateInbox(InboxBody body) {
    if (body == null
        || !StringUtils.hasText(body.name())
        || !StringUtils.hasText(body.host())
        || !StringUtils.hasText(body.username())
        || body.port() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid inbox");
    }
  }

  private static String blankToNull(String s) {
    return StringUtils.hasText(s) ? s.trim() : null;
  }

  public record InboxBody(
      String name,
      String host,
      int port,
      String username,
      String password,
      boolean useSsl,
      int pollIntervalMinutes,
      Boolean enabled) {}

  public record ConnectionTestRequest(
      String host, int port, String username, String password, boolean useSsl, String inboxId) {}

  public record ConnectionTestResponse(boolean ok, String message) {}

  public record SenderBody(String email) {}

  public record MappingBody(String name, List<String> listIds, String substituteFrom, String footerId) {}

  public record InboxSummary(String id, String name, boolean enabled, int pollIntervalMinutes) {}

  public record InboxDetail(
      String id,
      String name,
      String host,
      int port,
      String username,
      boolean useSsl,
      int pollIntervalMinutes,
      boolean enabled,
      List<String> authorizedSenders,
      List<MappingSummary> mappings) {}

  public record MappingSummary(
      String id,
      String inboxId,
      String name,
      String substituteFrom,
      String footerId,
      List<String> listIds) {}

  public record IssueView(
      String id,
      String inboxId,
      String mappingId,
      String senderEmail,
      String subject,
      String status,
      String resolution,
      String detail,
      String createdAt) {}

  public record DropView(
      String id,
      String inboxId,
      String senderEmail,
      String reason,
      boolean countedForAutoBlock,
      String createdAt,
      boolean resolved) {}

  public record BlockView(String id, String kind, String value, String source) {}
}
