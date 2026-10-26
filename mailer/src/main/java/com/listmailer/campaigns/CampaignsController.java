package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

  public CampaignsController(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      CampaignAttachmentRepository attachments,
      ListRepository lists,
      ListAccessService access,
      CampaignSendService sendService,
      SendQueueRepository queue) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
    this.attachments = attachments;
    this.lists = lists;
    this.access = access;
    this.sendService = sendService;
    this.queue = queue;
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
    return detail(sendService.queueAndSend(id));
  }

  @PostMapping("/{id}/test-send")
  public TestSendResult testSend(@PathVariable String id, @RequestBody TestSendBody body) {
    if (body == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "addresses required");
    }
    int sent = sendService.testSend(id, body.addresses());
    return new TestSendResult(sent);
  }

  @PostMapping("/{id}/stop")
  @Transactional
  public CampaignDetail stop(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.DRAFT
        && c.getStatus() != CampaignStatus.QUEUED
        && c.getStatus() != CampaignStatus.SENDING) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot stop from " + c.getStatus());
    }
    c.setStatus(CampaignStatus.STOPPED);
    return detail(campaigns.save(c));
  }

  @PostMapping("/{id}/requeue")
  public CampaignDetail requeue(@PathVariable String id) {
    CampaignEntity c = require(id);
    if (c.getStatus() != CampaignStatus.STOPPED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "requeue only from Stopped");
    }
    return detail(sendService.queueAndSend(id));
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
      byte[] bytes;
      try {
        bytes = Base64.getDecoder().decode(a.contentBase64());
      } catch (IllegalArgumentException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid attachment encoding");
      }
      String ct = StringUtils.hasText(a.contentType()) ? a.contentType() : "application/octet-stream";
      attachments.save(
          new CampaignAttachmentEntity(Guids.newGuid(), campaignId, a.filename().trim(), ct, bytes));
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
    List<String> listIds =
        campaignLists.findByCampaignIdOrderByPositionAscListIdAsc(c.getId()).stream()
            .map(CampaignListEntity::getListId)
            .toList();
    long sent =
        queue.findByCampaignId(c.getId()).stream().filter(r -> "SENT".equals(r.getStatus())).count();
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
        files);
  }

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

  public record CampaignDetail(
      String id,
      String subject,
      String htmlBody,
      String textBody,
      String status,
      List<String> listIds,
      long sentCount,
      List<AttachmentOut> attachments) {}
}
