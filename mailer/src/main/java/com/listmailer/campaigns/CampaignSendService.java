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
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CampaignSendService {

  private final CampaignRepository campaigns;
  private final CampaignListRepository campaignLists;
  private final SendQueueRepository queue;
  private final ListRepository lists;
  private final ListMemberRepository listMembers;
  private final MemberRepository members;
  private final SmtpPort smtp;

  public CampaignSendService(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      SendQueueRepository queue,
      ListRepository lists,
      ListMemberRepository listMembers,
      MemberRepository members,
      SmtpPort smtp) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
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
    List<CampaignListEntity> linked = campaignLists.findByCampaignId(campaignId);
    if (linked.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "campaign has no lists");
    }
    // first list determines sending path (AD)
    ListEntity firstList =
        lists
            .findById(linked.get(0).getListId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "list missing"));
    String pathId = firstList.getSendingPathId();

    Map<String, MemberEntity> unique = new LinkedHashMap<>();
    for (CampaignListEntity cl : linked) {
      for (ListMemberEntity lm : listMembers.findByListId(cl.getListId())) {
        if (lm.isUnsubscribed()) {
          continue;
        }
        members
            .findById(lm.getMemberId())
            .filter(MemberEntity::isEnabled)
            .ifPresent(m -> unique.putIfAbsent(m.getId(), m));
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
              campaign.getHtmlBody()));
      row.setStatus("SENT");
      queue.save(row);
    }
    campaign.setStatus(CampaignStatus.FINISHED);
    return campaigns.save(campaign);
  }
}
