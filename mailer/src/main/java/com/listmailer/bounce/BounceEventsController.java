package com.listmailer.bounce;

import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.SendQueueEntity;
import com.listmailer.campaigns.SendQueueRepository;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberRepository;
import com.listmailer.users.ListAccessService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Operator Bounce Messages index — reads existing {@code bounce_events} rows (including historical).
 */
@RestController
@RequestMapping("/api/bounce-events")
public class BounceEventsController {

  private final BounceEventRepository events;
  private final SendQueueRepository queue;
  private final CampaignRepository campaigns;
  private final MemberRepository members;
  private final ListMemberRepository listMembers;
  private final ListAccessService access;

  public BounceEventsController(
      BounceEventRepository events,
      SendQueueRepository queue,
      CampaignRepository campaigns,
      MemberRepository members,
      ListMemberRepository listMembers,
      ListAccessService access) {
    this.events = events;
    this.queue = queue;
    this.campaigns = campaigns;
    this.members = members;
    this.listMembers = listMembers;
    this.access = access;
  }

  @GetMapping
  @Transactional
  public List<BounceMessageView> index(@RequestParam(required = false) String filter) {
    requireOperator();
    // shortcut: load then filter in memory; add DB predicates when bounce_events grows large
    Stream<BounceEventEntity> stream = events.findAllByOrderByReceivedAtDesc().stream();
    stream = applyRoleScope(stream);
    stream = applyFilter(stream, filter);
    return stream.limit(500).map(this::toView).toList();
  }

  @GetMapping("/{id}")
  @Transactional
  public BounceMessageDetail get(@PathVariable String id) {
    requireOperator();
    BounceEventEntity ev =
        events.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!visible(ev)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return toDetail(ev);
  }

  private void requireOperator() {
    if (access.isListSenderOnly()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
    }
    if (!access.isAdmin() && !access.isListManager()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
    }
  }

  private Stream<BounceEventEntity> applyRoleScope(Stream<BounceEventEntity> stream) {
    if (access.isAdmin()) {
      return stream;
    }
    Set<String> allowed = managedMemberIds();
    return stream.filter(e -> e.getMemberId() != null && allowed.contains(e.getMemberId()));
  }

  private boolean visible(BounceEventEntity ev) {
    if (access.isAdmin()) {
      return true;
    }
    return ev.getMemberId() != null && managedMemberIds().contains(ev.getMemberId());
  }

  private Set<String> managedMemberIds() {
    List<String> lists = access.managedListIds();
    if (lists.isEmpty()) {
      return Set.of();
    }
    Set<String> ids = new HashSet<>();
    listMembers.findByListIdIn(lists).forEach(lm -> ids.add(lm.getMemberId()));
    return ids;
  }

  private static Stream<BounceEventEntity> applyFilter(
      Stream<BounceEventEntity> stream, String filter) {
    if (!StringUtils.hasText(filter) || "all".equalsIgnoreCase(filter.trim())) {
      return stream;
    }
    String f = filter.trim().toLowerCase(Locale.ROOT);
    return switch (f) {
      case "correlated" ->
          stream.filter(
              e ->
                  "CORRELATED".equals(e.getStatus())
                      || ("PROCESSED".equals(e.getStatus()) && e.getSendQueueId() != null));
      case "uncorrelated" -> stream.filter(e -> "UNCORRELATED".equals(e.getStatus()));
      case "hard" -> stream.filter(e -> "HARD".equalsIgnoreCase(e.getClassification()));
      case "soft" -> stream.filter(e -> "SOFT".equalsIgnoreCase(e.getClassification()));
      default -> stream;
    };
  }

  private BounceMessageView toView(BounceEventEntity ev) {
    ensureErrorText(ev);
    String email = null;
    String campaignId = null;
    String campaignSubject = null;
    if (StringUtils.hasText(ev.getMemberId())) {
      email = members.findById(ev.getMemberId()).map(m -> m.getEmail()).orElse(null);
    }
    if (StringUtils.hasText(ev.getSendQueueId())) {
      SendQueueEntity row = queue.findById(ev.getSendQueueId()).orElse(null);
      if (row != null) {
        if (!StringUtils.hasText(email)) {
          email = row.getEmail();
        }
        campaignId = row.getCampaignId();
        if (StringUtils.hasText(campaignId)) {
          campaignSubject =
              campaigns.findById(campaignId).map(c -> c.getSubject()).orElse(null);
        }
      }
    }
    boolean correlated =
        "CORRELATED".equals(ev.getStatus())
            || ("PROCESSED".equals(ev.getStatus()) && ev.getSendQueueId() != null);
    return new BounceMessageView(
        ev.getId(),
        ev.getReceivedAt(),
        email,
        campaignId,
        campaignSubject,
        ev.getClassification(),
        ev.getErrorText(),
        correlated,
        ev.getStatus(),
        ev.getMemberId(),
        ev.getCorrelationSignal(),
        ev.getSubject(),
        ev.getMailboxId());
  }

  private BounceMessageDetail toDetail(BounceEventEntity ev) {
    BounceMessageView summary = toView(ev);
    String headers = ev.getHeadersJson() == null ? "" : ev.getHeadersJson();
    String payload =
        ev.getRawBytes() == null ? "" : new String(ev.getRawBytes(), StandardCharsets.UTF_8);
    return new BounceMessageDetail(summary, headers, payload, ev.getFromAddr(), ev.getMessageId());
  }

  /** Backfill error_text for rows ingested before the column existed. */
  private void ensureErrorText(BounceEventEntity ev) {
    if (StringUtils.hasText(ev.getErrorText())) {
      return;
    }
    String blob = textOf(ev);
    String error = BounceErrorExtractor.extract(blob, ev.getSubject());
    ev.setErrorText(error);
    if (!StringUtils.hasText(ev.getClassification()) && StringUtils.hasText(blob)) {
      ev.setClassification(BounceCorrelationService.classify(blob));
    }
    events.save(ev);
  }

  private static String textOf(BounceEventEntity ev) {
    StringBuilder sb = new StringBuilder();
    if (ev.getHeadersJson() != null) {
      sb.append(ev.getHeadersJson()).append('\n');
    }
    if (ev.getSubject() != null) {
      sb.append(ev.getSubject()).append('\n');
    }
    if (ev.getRawBytes() != null) {
      sb.append(new String(ev.getRawBytes(), StandardCharsets.UTF_8));
    }
    return sb.toString();
  }

  public record BounceMessageView(
      String id,
      Instant receivedAt,
      String email,
      String campaignId,
      String campaignSubject,
      String classification,
      String error,
      boolean correlated,
      String status,
      String memberId,
      String correlationSignal,
      String subject,
      String mailboxId) {}

  /** Detail includes stored headers and raw RFC822 payload from {@code bounce_events}. */
  public record BounceMessageDetail(
      BounceMessageView summary,
      String headers,
      String payload,
      String fromAddr,
      String messageId) {}
}
