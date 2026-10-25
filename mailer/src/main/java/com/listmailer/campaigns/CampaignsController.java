package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignsController {

  private final CampaignRepository campaigns;
  private final CampaignListRepository campaignLists;
  private final ListRepository lists;
  private final ListAccessService access;
  private final CampaignSendService sendService;
  private final SendQueueRepository queue;

  public CampaignsController(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      ListRepository lists,
      ListAccessService access,
      CampaignSendService sendService,
      SendQueueRepository queue) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
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
    if (body == null
        || !StringUtils.hasText(body.subject())
        || body.htmlBody() == null
        || body.textBody() == null
        || body.listIds() == null
        || body.listIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid campaign");
    }
    for (String listId : body.listIds()) {
      if (!lists.existsById(listId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown list");
      }
      if (!access.canSend(listId) && !access.canManage(listId) && !access.isAdmin()) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "send grant required");
      }
    }
    String id = Guids.newGuid();
    CampaignEntity saved =
        campaigns.save(
            new CampaignEntity(
                id,
                body.subject().trim(),
                body.htmlBody(),
                body.textBody(),
                CampaignStatus.DRAFT,
                Instant.now()));
    for (String listId : body.listIds()) {
      campaignLists.save(new CampaignListEntity(id, listId));
    }
    return detail(saved);
  }

  @GetMapping("/{id}")
  public CampaignDetail get(@PathVariable String id) {
    return detail(require(id));
  }

  @PostMapping("/{id}/queue")
  public CampaignDetail queue(@PathVariable String id) {
    return detail(sendService.queueAndSend(id));
  }

  private CampaignEntity require(String id) {
    return campaigns
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
  }

  private CampaignDetail detail(CampaignEntity c) {
    List<String> listIds =
        campaignLists.findByCampaignId(c.getId()).stream()
            .map(CampaignListEntity::getListId)
            .toList();
    long sent =
        queue.findByCampaignId(c.getId()).stream().filter(r -> "SENT".equals(r.getStatus())).count();
    return new CampaignDetail(
        c.getId(), c.getSubject(), c.getStatus().name(), listIds, sent);
  }

  public record CreateBody(
      String subject, String htmlBody, String textBody, List<String> listIds) {}

  public record CampaignSummary(String id, String subject, String status) {
    static CampaignSummary from(CampaignEntity c) {
      return new CampaignSummary(c.getId(), c.getSubject(), c.getStatus().name());
    }
  }

  public record CampaignDetail(
      String id, String subject, String status, List<String> listIds, long sentCount) {}
}
