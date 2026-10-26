package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CampaignSendService {

  private static final Pattern EMAIL =
      Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  private final CampaignRepository campaigns;
  private final CampaignListRepository campaignLists;
  private final CampaignAttachmentRepository attachments;
  private final SendQueueRepository queue;
  private final ListRepository lists;
  private final ListMemberRepository listMembers;
  private final MemberRepository members;
  private final SmtpPort smtp;
  private final OpsHooksPort ops;
  private final ParametersPort parameters;
  private final CampaignHtmlRewriter htmlRewriter;

  public CampaignSendService(
      CampaignRepository campaigns,
      CampaignListRepository campaignLists,
      CampaignAttachmentRepository attachments,
      SendQueueRepository queue,
      ListRepository lists,
      ListMemberRepository listMembers,
      MemberRepository members,
      SmtpPort smtp,
      OpsHooksPort ops,
      ParametersPort parameters,
      CampaignHtmlRewriter htmlRewriter) {
    this.campaigns = campaigns;
    this.campaignLists = campaignLists;
    this.attachments = attachments;
    this.queue = queue;
    this.lists = lists;
    this.listMembers = listMembers;
    this.members = members;
    this.smtp = smtp;
    this.ops = ops;
    this.parameters = parameters;
    this.htmlRewriter = htmlRewriter;
  }

  /** Test send: pasted addresses, first-list path, no queue rows / no stats. Ignores Member disable. */
  public int testSend(String campaignId, List<String> rawAddresses) {
    CampaignEntity campaign =
        campaigns
            .findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
    if (campaign.getStatus() != CampaignStatus.DRAFT) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "test send only from Draft");
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
    List<String> addresses = normalizeAddresses(rawAddresses);
    if (addresses.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paste at least one valid address");
    }
    ListEntity firstList =
        lists
            .findById(linked.get(0).getListId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "list missing"));
    String pathId = firstList.getSendingPathId();
    List<CampaignAttachmentEntity> blobs = attachments.findByCampaignId(campaignId);
    List<SmtpPort.Attachment> files =
        blobs.stream()
            .map(
                a ->
                    new SmtpPort.Attachment(
                        a.getFilename(), a.getContentType(), a.getContent()))
            .toList();
    String html = rewriteHtml(campaignId, campaign.getHtmlBody(), blobs);
    int sent = 0;
    for (String to : addresses) {
      try {
        smtp.send(
            new SmtpPort.SmtpMessage(
                pathId,
                to,
                "[TEST] " + campaign.getSubject(),
                campaign.getTextBody(),
                html,
                files));
        sent++;
      } catch (RuntimeException ex) {
        ops.opsLog("send.test", "test send failed to " + to + ": " + ex.getMessage());
        ops.attentionAdmin("Test send failed", "Campaign " + campaignId + " → " + to);
        throw new ResponseStatusException(
            HttpStatus.BAD_GATEWAY, "test send failed: " + ex.getMessage(), ex);
      }
    }
    return sent;
  }

  private String rewriteHtml(
      String campaignId, String html, List<CampaignAttachmentEntity> blobs) {
    if (blobs == null || blobs.isEmpty()) {
      return html == null ? "" : html;
    }
    String base =
        parameters
            .get(ParameterKeys.PUBLIC_BASE_URL)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "public.base.url missing"));
    return htmlRewriter.rewrite(
        html, campaignId, base, blobs.stream().map(CampaignAttachmentEntity::getFilename).toList());
  }

  private static List<String> normalizeAddresses(List<String> raw) {
    LinkedHashSet<String> out = new LinkedHashSet<>();
    if (raw == null) {
      return List.of();
    }
    for (String line : raw) {
      if (!StringUtils.hasText(line)) {
        continue;
      }
      for (String part : line.split("[,;\\s]+")) {
        String email = part.trim();
        if (!StringUtils.hasText(email)) {
          continue;
        }
        if (!EMAIL.matcher(email).matches()) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid address: " + email);
        }
        out.add(email);
      }
    }
    return new ArrayList<>(out);
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
    List<CampaignAttachmentEntity> blobs = attachments.findByCampaignId(campaignId);
    List<SmtpPort.Attachment> files =
        blobs.stream()
            .map(
                a ->
                    new SmtpPort.Attachment(
                        a.getFilename(), a.getContentType(), a.getContent()))
            .toList();
    String html = rewriteHtml(campaignId, campaign.getHtmlBody(), blobs);
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
              html,
              files));
      row.setStatus("SENT");
      queue.save(row);
    }
    campaign.setStatus(CampaignStatus.FINISHED);
    return campaigns.save(campaign);
  }
}
