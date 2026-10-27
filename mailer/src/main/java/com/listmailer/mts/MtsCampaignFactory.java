package com.listmailer.mts;

import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignSendService;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.catalogs.FooterEntity;
import com.listmailer.catalogs.FooterRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/** Creates MTS Campaigns then queues them (skip Draft UX — status becomes Queued). */
@Service
public class MtsCampaignFactory {

  private final CampaignRepository campaigns;
  private final CampaignListRepository campaignLists;
  private final CampaignSendService sendService;
  private final FooterRepository footers;
  private final ListRepository lists;

  public MtsCampaignFactory(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      CampaignSendService sendService,
      FooterRepository footers,
      ListRepository lists) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
    this.sendService = sendService;
    this.footers = footers;
    this.lists = lists;
  }

  @Transactional
  public CampaignEntity createQueued(
      String subject,
      String htmlBody,
      String textBody,
      List<MtsSenderListGrantEntity> grants) {
    return create(subject, htmlBody, textBody, grants, true);
  }

  /**
   * Creates an MTS campaign. When {@code autoQueue} is false the campaign stays Draft (operator
   * queues later).
   */
  @Transactional
  public CampaignEntity create(
      String subject,
      String htmlBody,
      String textBody,
      List<MtsSenderListGrantEntity> grants,
      boolean autoQueue) {
    if (grants == null || grants.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sender has no list grants");
    }
    String html = htmlBody == null ? "" : htmlBody;
    String text = textBody == null ? "" : textBody;
    // shortcut: multi-list different footers use first grant's footer, split campaigns when that hurts
    html = appendFooter(html, grants.get(0));
    String id = Guids.newGuid();
    CampaignEntity c =
        campaigns.save(
            new CampaignEntity(
                id,
                StringUtils.hasText(subject) ? subject.trim() : "(no subject)",
                html,
                text,
                CampaignStatus.DRAFT,
                Instant.now()));
    int i = 0;
    for (MtsSenderListGrantEntity g : grants) {
      campaignLists.save(new CampaignListEntity(id, g.getListId(), i++));
    }
    if (!autoQueue) {
      return c;
    }
    return sendService.queueAndSend(id);
  }

  private String appendFooter(String html, MtsSenderListGrantEntity grant) {
    String footerId = grant.getFooterId();
    if (!StringUtils.hasText(footerId)) {
      footerId = lists.findById(grant.getListId()).map(ListEntity::getFooterId).orElse(null);
    }
    if (!StringUtils.hasText(footerId)) {
      return html;
    }
    FooterEntity f = footers.findById(footerId).orElse(null);
    if (f == null || !StringUtils.hasText(f.getHtmlBody())) {
      return html;
    }
    return html + f.getHtmlBody();
  }
}
