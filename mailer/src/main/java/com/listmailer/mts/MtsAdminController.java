package com.listmailer.mts;

import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.lists.ListRepository;
import com.listmailer.mailbox.MailConnectionProbe;
import com.listmailer.mailbox.MailboxPollLock;
import com.listmailer.mailbox.MailSecurity;
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
  private static final String REF_SOURCE = "MTS_SENDER_GRANT";

  private final MtsInboxRepository inboxes;
  private final MtsAuthorizedSenderRepository senders;
  private final MtsSenderListGrantRepository grants;
  private final ListRepository lists;
  private final MtsFromDomainService fromDomain;
  private final MtsDropService drops;
  private final CatalogReferenceService references;
  private final MailConnectionProbe probe;
  private final MtsIngestService ingest;
  private final MailboxPollLock locks;

  public MtsAdminController(
      MtsInboxRepository inboxes,
      MtsAuthorizedSenderRepository senders,
      MtsSenderListGrantRepository grants,
      ListRepository lists,
      MtsFromDomainService fromDomain,
      MtsDropService drops,
      CatalogReferenceService references,
      MailConnectionProbe probe,
      MtsIngestService ingest,
      MailboxPollLock locks) {
    this.inboxes = inboxes;
    this.senders = senders;
    this.grants = grants;
    this.lists = lists;
    this.fromDomain = fromDomain;
    this.drops = drops;
    this.references = references;
    this.probe = probe;
    this.ingest = ingest;
    this.locks = locks;
  }

  @GetMapping("/drop-log")
  public List<DropView> dropLog(
      @org.springframework.web.bind.annotation.RequestParam(required = false) String inboxId) {
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
        .map(
            i ->
                new InboxSummary(
                    i.getId(),
                    i.getName(),
                    i.isEnabled(),
                    i.getPollIntervalMinutes(),
                    i.getLastPolledAt() == null ? null : i.getLastPolledAt().toString(),
                    pollResult(i)))
        .toList();
  }

  @PostMapping("/inboxes/{id}/poll")
  public Map<String, String> pollNow(@PathVariable String id) {
    MtsInboxEntity box = requireInbox(id);
    if (!box.isEnabled()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "mailbox is disabled");
    }
    ingest.startPollAsync(id);
    return Map.of("message", "process started");
  }

  @PostMapping("/inboxes/{id}/break-lock")
  public InboxDetail breakLock(@PathVariable String id) {
    requireInbox(id);
    ingest.breakLock(id);
    return detail(requireInbox(id));
  }

  @PostMapping("/inboxes/test-connection")
  public ConnectionTestResponse testInboxConnection(@RequestBody ConnectionTestRequest body) {
    if (body == null
        || !StringUtils.hasText(body.host())
        || body.port() <= 0
        || !StringUtils.hasText(body.username())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "host, port, and username are required");
    }
    String password = body.password();
    if (!StringUtils.hasText(password) && StringUtils.hasText(body.inboxId())) {
      password = requireInbox(body.inboxId()).getPassword();
    }
    MailConnectionProbe.Result result =
        probe.testImap(
            body.host(), body.port(), body.username(), password, body.useSsl(), body.useTls());
    return new ConnectionTestResponse(result.ok(), result.message());
  }

  @PostMapping("/inboxes")
  public InboxDetail createInbox(@RequestBody InboxBody body) {
    validateInbox(body, true);
    int poll = body.pollIntervalMinutes() > 0 ? body.pollIntervalMinutes() : DEFAULT_POLL;
    MtsInboxEntity box =
        new MtsInboxEntity(
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
    if (body.autoQueue() != null) {
      box.setAutoQueue(body.autoQueue());
    }
    if (body.deleteAfterAccept() != null) {
      box.setDeleteAfterAccept(body.deleteAfterAccept());
    }
    MtsInboxEntity saved = inboxes.save(box);
    return detail(saved);
  }

  @GetMapping("/inboxes/{id}")
  public InboxDetail getInbox(@PathVariable String id) {
    return detail(requireInbox(id));
  }

  @PutMapping("/inboxes/{id}")
  public InboxDetail updateInbox(@PathVariable String id, @RequestBody InboxBody body) {
    validateInbox(body, false);
    MtsInboxEntity box = requireInbox(id);
    box.setName(body.name().trim());
    box.setHost(body.host().trim());
    box.setPort(body.port());
    box.setUsername(body.username());
    if (StringUtils.hasText(body.password())) {
      box.setPassword(body.password());
    }
    box.setUseSsl(body.useSsl());
    box.setUseTls(body.useTls());
    if (body.pollIntervalMinutes() > 0) {
      box.setPollIntervalMinutes(body.pollIntervalMinutes());
    }
    if (body.enabled() != null) {
      box.setEnabled(body.enabled());
    }
    if (body.autoQueue() != null) {
      box.setAutoQueue(body.autoQueue());
    }
    if (body.deleteAfterAccept() != null) {
      box.setDeleteAfterAccept(body.deleteAfterAccept());
    }
    return detail(inboxes.save(box));
  }

  @PostMapping("/inboxes/{id}/delete-seen")
  public Map<String, Object> deleteSeen(@PathVariable String id) {
    requireInbox(id);
    try {
      int deleted = ingest.deleteSeenMessages(id);
      return Map.of("deleted", deleted, "message", "deleted " + deleted + " seen message(s)");
    } catch (IllegalStateException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
    }
  }

  @DeleteMapping("/inboxes/{id}")
  @Transactional
  public Map<String, String> deleteInbox(@PathVariable String id) {
    MtsInboxEntity box = requireInbox(id);
    for (MtsAuthorizedSenderEntity sender : senders.findByInboxId(id)) {
      clearGrantReferences(id, sender.getEmail());
    }
    drops.deleteDropsForInbox(id);
    // DB cascades: senders → grants; from-domain issues
    inboxes.delete(box);
    return Map.of("status", "deleted");
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
    String norm = email.trim().toLowerCase(Locale.ROOT);
    clearGrantReferences(id, norm);
    grants.deleteByInboxIdAndSenderEmail(id, norm);
    senders.deleteByInboxIdAndEmail(id, norm);
    return detail(requireInbox(id));
  }

  @GetMapping("/inboxes/{id}/senders/{email}")
  public SenderDetail getSender(@PathVariable String id, @PathVariable String email) {
    requireInbox(id);
    String norm = email.trim().toLowerCase(Locale.ROOT);
    if (!senders.existsByInboxIdAndEmail(id, norm)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "sender not found");
    }
    return senderDetail(id, norm);
  }

  @PutMapping("/inboxes/{id}/senders/{email}/grants")
  @Transactional
  public SenderDetail putSenderGrants(
      @PathVariable String id, @PathVariable String email, @RequestBody GrantsBody body) {
    requireInbox(id);
    String norm = email.trim().toLowerCase(Locale.ROOT);
    if (!senders.existsByInboxIdAndEmail(id, norm)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "sender not found");
    }
    if (body == null || body.grants() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "grants required");
    }
    clearGrantReferences(id, norm);
    grants.deleteByInboxIdAndSenderEmail(id, norm);
    int pos = 0;
    for (GrantRow row : body.grants()) {
      if (row == null || !row.granted() || !StringUtils.hasText(row.listId())) {
        continue;
      }
      if (!lists.existsById(row.listId())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown list");
      }
      String footerId = blankToNull(row.footerId());
      MtsSenderListGrantEntity g =
          grants.save(
              new MtsSenderListGrantEntity(
                  Guids.newGuid(),
                  id,
                  norm,
                  row.listId(),
                  blankToNull(row.substituteFrom()),
                  footerId,
                  pos++));
      lists
          .findById(row.listId())
          .ifPresent(
              list ->
                  references.add(
                      CatalogTargetType.SENDING_PATH,
                      list.getSendingPathId(),
                      REF_SOURCE,
                      g.getId(),
                      "MTS sender " + norm + " → " + list.getName()));
      if (StringUtils.hasText(footerId)) {
        references.add(
            CatalogTargetType.FOOTER,
            footerId,
            REF_SOURCE,
            g.getId(),
            "MTS sender " + norm);
      }
    }
    return senderDetail(id, norm);
  }

  @GetMapping("/mappings")
  public List<MappingSummary> allMappings() {
    List<MappingSummary> out = new ArrayList<>();
    for (MtsSenderListGrantEntity g : grants.findAllByOrderByInboxIdAscSenderEmailAscPositionAsc()) {
      out.add(mappingSummary(g));
    }
    return out;
  }

  private void clearGrantReferences(String inboxId, String senderEmail) {
    for (MtsSenderListGrantEntity g :
        grants.findByInboxIdAndSenderEmailOrderByPositionAsc(inboxId, senderEmail)) {
      lists
          .findById(g.getListId())
          .ifPresent(
              list ->
                  references.remove(
                      CatalogTargetType.SENDING_PATH,
                      list.getSendingPathId(),
                      REF_SOURCE,
                      g.getId()));
      if (StringUtils.hasText(g.getFooterId())) {
        references.remove(CatalogTargetType.FOOTER, g.getFooterId(), REF_SOURCE, g.getId());
      }
    }
  }

  private InboxDetail detail(MtsInboxEntity box) {
    List<String> emails =
        senders.findByInboxId(box.getId()).stream().map(MtsAuthorizedSenderEntity::getEmail).toList();
    List<MappingSummary> maps =
        grants.findByInboxIdOrderBySenderEmailAscPositionAsc(box.getId()).stream()
            .map(this::mappingSummary)
            .toList();
    Instant now = Instant.now();
    boolean held = locks.stillHeld(box.isPolling(), box.getLastPollingUpdate(), now);
    return new InboxDetail(
        box.getId(),
        box.getName(),
        box.getHost(),
        box.getPort(),
        box.getUsername(),
        box.isUseSsl(),
        box.isUseTls(),
        box.getPollIntervalMinutes(),
        box.isEnabled(),
        box.isAutoQueue(),
        box.isDeleteAfterAccept(),
        emails,
        maps,
        box.getLastPolledAt() == null ? null : box.getLastPolledAt().toString(),
        box.getLastPollingUpdate() == null ? null : box.getLastPollingUpdate().toString(),
        pollResult(box),
        held);
  }

  private static String pollResult(MtsInboxEntity b) {
    if (b.getLastPollOk() == null) {
      return "";
    }
    return b.getLastPollOk() ? "OK" : "Errors";
  }

  private SenderDetail senderDetail(String inboxId, String email) {
    Map<String, MtsSenderListGrantEntity> byList =
        grants.findByInboxIdAndSenderEmailOrderByPositionAsc(inboxId, email).stream()
            .collect(java.util.stream.Collectors.toMap(MtsSenderListGrantEntity::getListId, g -> g, (a, b) -> a));
    List<GrantView> rows = new ArrayList<>();
    lists.findAll().stream()
        .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
        .forEach(
            list -> {
              MtsSenderListGrantEntity g = byList.get(list.getId());
              rows.add(
                  new GrantView(
                      list.getId(),
                      list.getName(),
                      g != null,
                      g == null ? null : g.getSubstituteFrom(),
                      g == null ? null : g.getFooterId()));
            });
    return new SenderDetail(email, rows);
  }

  private MappingSummary mappingSummary(MtsSenderListGrantEntity g) {
    String listName = lists.findById(g.getListId()).map(l -> l.getName()).orElse(g.getListId());
    return new MappingSummary(
        g.getId(),
        g.getInboxId(),
        g.getSenderEmail(),
        g.getListId(),
        listName,
        g.getSubstituteFrom(),
        g.getFooterId());
  }

  private MtsInboxEntity requireInbox(String id) {
    return inboxes
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "inbox not found"));
  }

  private static void validateInbox(InboxBody body, boolean requirePassword) {
    if (body == null
        || !StringUtils.hasText(body.name())
        || !StringUtils.hasText(body.host())
        || !StringUtils.hasText(body.username())
        || body.port() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid inbox");
    }
    if (requirePassword && !StringUtils.hasText(body.password())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password required");
    }
    try {
      MailSecurity.check(body.useSsl(), body.useTls());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
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
      boolean useTls,
      int pollIntervalMinutes,
      Boolean enabled,
      Boolean autoQueue,
      Boolean deleteAfterAccept) {}

  public record ConnectionTestRequest(
      String host,
      int port,
      String username,
      String password,
      boolean useSsl,
      boolean useTls,
      String inboxId) {}

  public record ConnectionTestResponse(boolean ok, String message) {}

  public record SenderBody(String email) {}

  public record GrantsBody(List<GrantRow> grants) {}

  public record GrantRow(String listId, boolean granted, String substituteFrom, String footerId) {}

  public record InboxSummary(
      String id,
      String name,
      boolean enabled,
      int pollIntervalMinutes,
      String lastPolledAt,
      String result) {}

  public record InboxDetail(
      String id,
      String name,
      String host,
      int port,
      String username,
      boolean useSsl,
      boolean useTls,
      int pollIntervalMinutes,
      boolean enabled,
      boolean autoQueue,
      boolean deleteAfterAccept,
      List<String> authorizedSenders,
      List<MappingSummary> mappings,
      String lastPolledAt,
      String lastPollingUpdate,
      String result,
      boolean polling) {}

  public record SenderDetail(String email, List<GrantView> grants) {}

  public record GrantView(
      String listId, String listName, boolean granted, String substituteFrom, String footerId) {}

  public record MappingSummary(
      String id,
      String inboxId,
      String senderEmail,
      String listId,
      String listName,
      String substituteFrom,
      String footerId) {}

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
