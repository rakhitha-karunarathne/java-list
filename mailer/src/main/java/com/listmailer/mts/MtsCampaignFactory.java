package com.listmailer.mts;

import com.listmailer.campaigns.CampaignAttachmentEntity;
import com.listmailer.campaigns.CampaignAttachmentRepository;
import com.listmailer.campaigns.CampaignEntity;
import com.listmailer.campaigns.CampaignListEntity;
import com.listmailer.campaigns.CampaignListRepository;
import com.listmailer.campaigns.CampaignRepository;
import com.listmailer.campaigns.CampaignSendService;
import com.listmailer.campaigns.CampaignStatus;
import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.catalogs.FooterEntity;
import com.listmailer.catalogs.FooterRepository;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.mailbox.MimeBodyParser;
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
  private final CampaignAttachmentRepository attachments;
  private final CampaignSendService sendService;
  private final FooterRepository footers;
  private final ListRepository lists;
  private final SendingPathRepository sendingPaths;
  private final CatalogReferenceService references;

  public MtsCampaignFactory(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      CampaignAttachmentRepository attachments,
      CampaignSendService sendService,
      FooterRepository footers,
      ListRepository lists,
      SendingPathRepository sendingPaths,
      CatalogReferenceService references) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
    this.attachments = attachments;
    this.sendService = sendService;
    this.footers = footers;
    this.lists = lists;
    this.sendingPaths = sendingPaths;
    this.references = references;
  }

  @Transactional
  public CampaignEntity createQueued(
      String subject,
      String htmlBody,
      String textBody,
      List<MtsSenderListGrantEntity> grants) {
    return create(subject, htmlBody, textBody, grants, true, List.of(), null);
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
    return create(subject, htmlBody, textBody, grants, autoQueue, List.of(), null);
  }

  @Transactional
  public CampaignEntity create(
      String subject,
      String htmlBody,
      String textBody,
      List<MtsSenderListGrantEntity> grants,
      boolean autoQueue,
      List<MimeBodyParser.Attachment> files) {
    return create(subject, htmlBody, textBody, grants, autoQueue, files, null);
  }

  @Transactional
  public CampaignEntity create(
      String subject,
      String htmlBody,
      String textBody,
      List<MtsSenderListGrantEntity> grants,
      boolean autoQueue,
      List<MimeBodyParser.Attachment> files,
      String sendingPathId) {
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
    if (StringUtils.hasText(sendingPathId)) {
      String pathId = sendingPathId.trim();
      if (!sendingPaths.existsById(pathId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown sending path");
      }
      c.setSendingPathId(pathId);
      campaigns.save(c);
      references.add(
          CatalogTargetType.SENDING_PATH, pathId, "CAMPAIGN", c.getId(), c.getSubject());
    }
    int i = 0;
    for (MtsSenderListGrantEntity g : grants) {
      campaignLists.save(new CampaignListEntity(id, g.getListId(), i++));
    }
    if (files != null) {
      for (MimeBodyParser.Attachment a : files) {
        if (a == null || a.content() == null || a.content().length == 0) {
          continue;
        }
        String name = StringUtils.hasText(a.filename()) ? a.filename() : "attachment.bin";
        String ct =
            StringUtils.hasText(a.contentType()) ? a.contentType() : "application/octet-stream";
        attachments.save(new CampaignAttachmentEntity(Guids.newGuid(), id, name, ct, a.content()));
      }
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
