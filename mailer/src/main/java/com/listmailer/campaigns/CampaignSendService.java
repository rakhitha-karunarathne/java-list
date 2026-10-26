package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CampaignSendService {

  private final CampaignRepository campaigns;
  private final CampaignListRepository campaignLists;
  private final CampaignAttachmentRepository attachments;
  private final SendQueueRepository queue;
  private final ListRepository lists;
  private final ListMemberRepository listMembers;
  private final MemberRepository members;
  private final SmtpPort smtp;

  public CampaignSendService(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      CampaignAttachmentRepository attachments,
      SendQueueRepository queue,
      ListRepository lists,
      ListMemberRepository listMembers,
      MemberRepository members,
      SmtpPort smtp) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
    this.attachments = attachments;
    this.queue = queue;
    this.lists = lists;
    this.listMembers = listMembers;
    this.members = members;
    this.smtp = smtp;
  }

  @Transactional
  public CampaignEntity queueAndSend(String campaignId) {
    CampaignEntity campaign =
        campaigns
            .findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
    if (campaign.getStatus() != CampaignStatus.DRAFT
        && campaign.getStatus() != CampaignStatus.STOPPED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "campaign not queueable");
    }
    if (!StringUtils.hasText(campaign.getHtmlBody())
        && !StringUtils.hasText(campaign.getTextBody())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "html or text body required");
    }
    List<CampaignListEntity> linked =
        campaignLists.findByCampaignIdOrderByPositionAscListIdAsc(campaignId);
    if (linked.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "campaign has no lists");
    }
    // first ordered list determines sending path (AD)
    ListEntity firstList =
        lists
            .findById(linked.get(0).getListId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "list missing"));
    String pathId = firstList.getSendingPathId();

    // address-level dedupe (R1): same email → one queue row
    Map<String, MemberEntity> unique = new LinkedHashMap<>();
    for (CampaignListEntity cl : linked) {
      for (ListMemberEntity lm : listMembers.findByListId(cl.getListId())) {
        if (lm.isUnsubscribed()) {
          continue;
        }
        members
            .findById(lm.getMemberId())
            .filter(MemberEntity::isEnabled)
            .ifPresent(
                m -> unique.putIfAbsent(m.getEmail().trim().toLowerCase(Locale.ROOT), m));
      }
    }
    campaign.setStatus(CampaignStatus.QUEUED);
    campaigns.save(campaign);
    for (MemberEntity member : unique.values()) {
      queue.save(
          new SendQueueEntity(
              Guids.newGuid(),
              campaignId,
              member.getId(),
              member.getEmail(),
              pathId,
              "PENDING"));
    }
    List<SmtpPort.Attachment> files =
        attachments.findByCampaignId(campaignId).stream()
            .map(
                a ->
                    new SmtpPort.Attachment(
                        a.getFilename(), a.getContentType(), a.getContent()))
            .toList();
    campaign.setStatus(CampaignStatus.SENDING);
    campaigns.save(campaign);
    for (SendQueueEntity row : queue.findByCampaignId(campaignId)) {
      if (!"PENDING".equals(row.getStatus())) {
        continue;
      }
      smtp.send(
          new SmtpPort.SmtpMessage(
              pathId,
              row.getEmail(),
              campaign.getSubject(),
              campaign.getTextBody(),
              campaign.getHtmlBody(),
              files));
      row.setStatus("SENT");
      queue.save(row);
    }
    campaign.setStatus(CampaignStatus.FINISHED);
    return campaigns.save(campaign);
  }
}
