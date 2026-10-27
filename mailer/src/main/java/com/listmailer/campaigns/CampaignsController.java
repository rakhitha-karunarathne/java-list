package com.listmailer.campaigns;

import com.listmailer.bounce.BounceEventRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignsController {

  private final CampaignRepository campaigns;
  private final CampaignListRepository campaignLists;
  private final CampaignAttachmentRepository attachments;
  private final ListRepository lists;
  private final ListAccessService access;
  private final CampaignSendService sendService;
  private final SendQueueRepository queue;
  private final CampaignOpenRepository opens;
  private final BounceEventRepository bounces;
  private final OpsHooksPort ops;
  private final SendPauseCache pauseCache;

  public CampaignsController(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      CampaignAttachmentRepository attachments,
      ListRepository lists,
      ListAccessService access,
      CampaignSendService sendService,
      SendQueueRepository queue,
      CampaignOpenRepository opens,
      BounceEventRepository bounces,
      OpsHooksPort ops,
      SendPauseCache pauseCache) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
    this.attachments = attachments;
    this.lists = lists;
    this.access = access;
    this.ops = ops;
    this.sendService = sendService;
    this.queue = queue;
    this.opens = opens;
    this.bounces = bounces;
    this.pauseCache = pauseCache;
  }

  @GetMapping
  public List<CampaignSummary> index() {
    return campaigns.findAll().stream().map(CampaignSummary::from).toList();
  }

  @PostMapping
  @Transactional
  public CampaignDetail create(@RequestBody CreateBody body) {
    if (body == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid campaign");
    }
    validateCompose(body.subject(), body.htmlBody(), body.textBody(), body.listIds(), true);
    String id = Guids.newGuid();
    CampaignEntity saved =
        campaigns.save(
            new CampaignEntity(
                id,
                body.subject().trim(),
                nullToEmpty(body.htmlBody()),
                nullToEmpty(body.textBody()),
                CampaignStatus.DRAFT,
                Instant.now()));
    replaceLists(id, body.listIds());
    if (body.attachments() != null) {
      replaceAttachments(id, body.attachments());
    }
    return detail(saved);
  }

  @PutMapping("/{id}")
  @Transactional
  public CampaignDetail update(@PathVariable String id, @RequestBody CreateBody body) {
    if (body == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid campaign");
    }
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.DRAFT) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "only Draft is editable");
    }
    validateCompose(body.subject(), body.htmlBody(), body.textBody(), body.listIds(), false);
    c.setSubject(body.subject().trim());
    c.setHtmlBody(nullToEmpty(body.htmlBody()));
    c.setTextBody(nullToEmpty(body.textBody()));
    campaigns.save(c);
    // null listIds/attachments = leave unchanged; empty list = clear
    if (body.listIds() != null) {
      replaceLists(id, body.listIds());
    }
    if (body.attachments() != null) {
      replaceAttachments(id, body.attachments());
    }
    return detail(c);
  }

  @GetMapping("/{id}")
  public CampaignDetail get(@PathVariable String id) {
    return detail(require(id));
  }

  @PostMapping("/{id}/queue")
  public CampaignDetail queue(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.DRAFT) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "queue only from Draft");
    }
    CampaignDetail out = detail(sendService.queueAndSend(id));
    ops.audit("campaign.queue", "campaign=" + id, actor(), null, null);
    return out;
  }

  @PostMapping("/{id}/test-send")
  public TestSendResult testSend(@PathVariable String id, @RequestBody TestSendBody body) {
    if (body == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "addresses required");
    }
    int sent = sendService.testSend(id, body.addresses());
    return new TestSendResult(sent);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Transactional
  public void delete(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() == CampaignStatus.SENDING || c.getStatus() == CampaignStatus.QUEUED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot delete while Queued/Sending");
    }
    attachments.deleteByCampaignId(id);
    queue.findByCampaignId(id).forEach(queue::delete);
    campaignLists.deleteByCampaignId(id);
    campaigns.delete(c);
  }

  @PostMapping("/{id}/stop")
  @Transactional
  public CampaignDetail stop(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.DRAFT
        && c.getStatus() != CampaignStatus.QUEUED
        && c.getStatus() != CampaignStatus.SENDING
        && c.getStatus() != CampaignStatus.PAUSED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot stop from " + c.getStatus());
    }
    c.setStatus(CampaignStatus.STOPPED);
    CampaignDetail out = detail(campaigns.save(c));
    ops.audit("campaign.stop", "campaign=" + id, actor(), null, null);
    return out;
  }

  @PostMapping("/{id}/pause")
  @Transactional
  public CampaignDetail pause(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.QUEUED && c.getStatus() != CampaignStatus.SENDING) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "pause only from Queued or Sending");
    }
    c.setStatus(CampaignStatus.PAUSED);
    CampaignDetail out = detail(campaigns.save(c));
    pauseCache.clearCampaign(id);
    ops.audit("campaign.pause", "campaign=" + id, actor(), null, null);
    return out;
  }

  @PostMapping("/{id}/resume")
  @Transactional
  public CampaignDetail resume(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.PAUSED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "resume only from Paused");
    }
    c.setStatus(CampaignStatus.QUEUED);
    CampaignDetail out = detail(campaigns.save(c));
    pauseCache.clearCampaign(id);
    ops.audit("campaign.resume", "campaign=" + id, actor(), null, null);
    return out;
  }

  @PostMapping("/{id}/lists")
  @Transactional
  public CampaignDetail addList(@PathVariable String id, @RequestBody AddListBody body) {
    if (body == null || !StringUtils.hasText(body.listId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "listId required");
    }
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.DRAFT
        && c.getStatus() != CampaignStatus.STOPPED
        && c.getStatus() != CampaignStatus.FINISHED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot add list while " + c.getStatus());
    }
    String listId = body.listId().trim();
    if (!lists.existsById(listId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown list");
    }
    if (!access.canSend(listId) && !access.canManage(listId) && !access.isAdmin()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "send grant required");
    }
    List<CampaignListEntity> linked =
        campaignLists.findByCampaignIdOrderByPositionAscListIdAsc(id);
    if (linked.stream().anyMatch(cl -> listId.equals(cl.getListId()))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "list already linked");
    }
    int pos = linked.isEmpty() ? 0 : linked.get(linked.size() - 1).getPosition() + 1;
    campaignLists.save(new CampaignListEntity(id, listId, pos));
    ops.audit("campaign.list_added", "campaign=" + id + " list=" + listId, actor(), listId, null);
    return detail(c);
  }

  @PostMapping("/{id}/requeue")
  public CampaignDetail requeue(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.STOPPED && c.getStatus() != CampaignStatus.FINISHED) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "requeue only from Stopped or Finished");
    }
    CampaignDetail out = detail(sendService.queueAndSend(id));
    ops.audit("campaign.requeue", "campaign=" + id, actor(), null, null);
    return out;
  }

  @PostMapping("/{id}/retry-failed")
  public CampaignDetail retryFailed(@PathVariable String id) {
    require(id);
    CampaignDetail out = detail(sendService.retryFailed(id));
    ops.audit("campaign.retry_failed", "campaign=" + id, actor(), null, null);
    return out;
  }

  private String actor() {
    return access.currentUser().map(u -> u.getEmail()).orElse("system");
  }

  private void validateCompose(
      String subject, String html, String text, List<String> listIds, boolean requireLists) {
    if (!StringUtils.hasText(subject)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subject required");
    }
    if (html == null || text == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "htmlBody and textBody required");
    }
    if (requireLists && (listIds == null || listIds.isEmpty())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one list required");
    }
    if (listIds != null) {
      if (listIds.size() != new HashSet<>(listIds).size()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "duplicate list");
      }
      for (String listId : listIds) {
        if (!lists.existsById(listId)) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown list");
        }
        if (!access.canSend(listId) && !access.canManage(listId) && !access.isAdmin()) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "send grant required");
        }
      }
    }
  }

  private void replaceLists(String campaignId, List<String> listIds) {
    campaignLists.deleteByCampaignId(campaignId);
    if (listIds == null) {
      return;
    }
    int i = 0;
    for (String listId : listIds) {
      campaignLists.save(new CampaignListEntity(campaignId, listId, i++));
    }
  }

  private void replaceAttachments(String campaignId, List<AttachmentIn> files) {
    attachments.deleteByCampaignId(campaignId);
    if (files == null) {
      return;
    }
    for (AttachmentIn a : files) {
      if (a == null || !StringUtils.hasText(a.filename()) || !StringUtils.hasText(a.contentBase64())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid attachment");
      }
      String name = a.filename().trim();
      if (name.contains("..") || name.contains("/") || name.contains("\\")) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid attachment filename");
      }
      byte[] bytes;
      try {
        bytes = Base64.getDecoder().decode(a.contentBase64());
      } catch (IllegalArgumentException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid attachment encoding");
      }
      String ct = StringUtils.hasText(a.contentType()) ? a.contentType() : "application/octet-stream";
      attachments.save(new CampaignAttachmentEntity(Guids.newGuid(), campaignId, name, ct, bytes));
    }
  }

  private static String nullToEmpty(String s) {
    return s == null ? "" : s;
  }

  private CampaignEntity require(String id) {
    return campaigns
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
  }

  private CampaignDetail detail(CampaignEntity c) {
    List<CampaignListEntity> linked =
        campaignLists.findByCampaignIdOrderByPositionAscListIdAsc(c.getId());
    List<String> listIds = linked.stream().map(CampaignListEntity::getListId).toList();
    List<CampaignListRef> listRefs =
        linked.stream()
            .map(
                cl ->
                    new CampaignListRef(
                        cl.getListId(),
                        lists.findById(cl.getListId()).map(l -> l.getName()).orElse("(missing)")))
            .toList();
    List<SendQueueEntity> rows = queue.findByCampaignId(c.getId());
    long sent = rows.stream().filter(r -> "SENT".equals(r.getStatus())).count();
    long waiting =
        rows.stream()
            .filter(
                r ->
                    "PENDING".equals(r.getStatus())
                        || "FAILED_RETRY_PENDING".equals(r.getStatus()))
            .count();
    Instant sendStartedAt =
        rows.stream().map(SendQueueEntity::getSentAt).filter(Objects::nonNull).min(Instant::compareTo).orElse(null);
    Instant sendFinishedAt =
        c.getStatus() == CampaignStatus.FINISHED
            ? rows.stream()
                .map(SendQueueEntity::getSentAt)
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null)
            : null;
    List<String> queueIds = rows.stream().map(SendQueueEntity::getId).toList();
    long bounceCount = queueIds.isEmpty() ? 0 : bounces.countBySendQueueIdIn(queueIds);
    java.util.Set<String> bouncedQueueIds =
        queueIds.isEmpty()
            ? java.util.Set.of()
            : bounces.findBySendQueueIdIn(queueIds).stream()
                .map(com.listmailer.bounce.BounceEventEntity::getSendQueueId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
    List<CampaignOpenEntity> openRows = opens.findByCampaignId(c.getId());
    long totalOpens = openRows.size();
    long uniqueOpens = openRows.stream().map(CampaignOpenEntity::getMemberId).distinct().count();
    var opensByQueue =
        openRows.stream()
            .collect(
                java.util.stream.Collectors.groupingBy(
                    CampaignOpenEntity::getSendQueueId,
                    java.util.LinkedHashMap::new,
                    java.util.stream.Collectors.toList()));
    List<RecipientStat> recipients =
        rows.stream()
            .map(
                r -> {
                  List<CampaignOpenEntity> hits = opensByQueue.getOrDefault(r.getId(), List.of());
                  Instant first =
                      hits.stream()
                          .map(CampaignOpenEntity::getOpenedAt)
                          .min(Instant::compareTo)
                          .orElse(null);
                  return new RecipientStat(
                      r.getEmail(),
                      recipientStatus(r.getStatus(), bouncedQueueIds.contains(r.getId()), !hits.isEmpty()),
                      first,
                      hits.size());
                })
            .sorted(Comparator.comparing(RecipientStat::email, String.CASE_INSENSITIVE_ORDER))
            .toList();
    List<MemberOpenStat> memberOpens =
        openRows.stream()
            .collect(
                java.util.stream.Collectors.groupingBy(
                    CampaignOpenEntity::getMemberId,
                    java.util.LinkedHashMap::new,
                    java.util.stream.Collectors.toList()))
            .entrySet()
            .stream()
            .map(
                e ->
                    new MemberOpenStat(
                        e.getKey(),
                        e.getValue().stream()
                            .map(CampaignOpenEntity::getOpenedAt)
                            .min(Instant::compareTo)
                            .orElse(null),
                        e.getValue().size()))
            .toList();
    List<AttachmentOut> files =
        attachments.findByCampaignId(c.getId()).stream()
            .map(a -> new AttachmentOut(a.getId(), a.getFilename(), a.getContentType(), a.getContent().length))
            .toList();
    return new CampaignDetail(
        c.getId(),
        c.getSubject(),
        c.getHtmlBody(),
        c.getTextBody(),
        c.getStatus().name(),
        listIds,
        sent,
        uniqueOpens,
        totalOpens,
        memberOpens,
        files,
        sendStartedAt,
        sendFinishedAt,
        waiting,
        bounceCount,
        uniqueOpens,
        totalOpens,
        listRefs,
        recipients);
  }

  public record AddListBody(String listId) {}

  public record TestSendBody(List<String> addresses) {}

  public record TestSendResult(int sent) {}

  public record AttachmentIn(String filename, String contentType, String contentBase64) {}

  public record AttachmentOut(String id, String filename, String contentType, int sizeBytes) {}

  public record CreateBody(
      String subject,
      String htmlBody,
      String textBody,
      List<String> listIds,
      List<AttachmentIn> attachments) {}

  public record CampaignSummary(String id, String subject, String status) {
    static CampaignSummary from(CampaignEntity c) {
      return new CampaignSummary(c.getId(), c.getSubject(), c.getStatus().name());
    }
  }

  public record MemberOpenStat(String memberId, java.time.Instant firstOpenAt, long openCount) {}

  public record CampaignListRef(String id, String name) {}

  public record RecipientStat(String email, String status, Instant firstOpenAt, long openCount) {}

  /** Display status for the recipients table. Bounced wins over Opened. */
  static String recipientStatus(String queueStatus, boolean bounced, boolean opened) {
    if ("PENDING".equals(queueStatus)) {
      return "Pending";
    }
    if ("FAILED_RETRY_PENDING".equals(queueStatus)) {
      return "Failed-Retry Pending";
    }
    if ("SEND_FAILED".equals(queueStatus)) {
      return "Send Failed";
    }
    if (bounced) {
      return "Bounced";
    }
    if (opened) {
      return "Opened";
    }
    if ("SENT".equals(queueStatus)) {
      return "Sent";
    }
    return queueStatus == null ? "" : queueStatus;
  }

  public record CampaignDetail(
      String id,
      String subject,
      String htmlBody,
      String textBody,
      String status,
      List<String> listIds,
      long sentCount,
      long uniqueOpens,
      long totalOpens,
      List<MemberOpenStat> memberOpens,
      List<AttachmentOut> attachments,
      Instant sendStartedAt,
      Instant sendFinishedAt,
      long waitingCount,
      long bounceCount,
      long uniqueViews,
      long totalViews,
      List<CampaignListRef> lists,
      List<RecipientStat> recipients) {}
}
