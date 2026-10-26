package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.kernel.token.OpaqueTokenHelper;
import com.listmailer.ops.ActivityLogService;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import com.listmailer.unsubscribe.UnsubTokenCodec;
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
  private final RemoteImageFetchService imageFetch;
  private final OpaqueTokenHelper opaqueTokens;
  private final SendingPathRepository sendingPaths;
  private final ActivityLogService activity;

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
      CampaignHtmlRewriter htmlRewriter,
      RemoteImageFetchService imageFetch,
      OpaqueTokenHelper opaqueTokens,
      SendingPathRepository sendingPaths,
      ActivityLogService activity) {
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
    this.imageFetch = imageFetch;
    this.opaqueTokens = opaqueTokens;
    this.sendingPaths = sendingPaths;
    this.activity = activity;
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
    return htmlRewriter.rewrite(
        html,
        campaignId,
        publicBase(),
        blobs.stream().map(CampaignAttachmentEntity::getFilename).toList());
  }

  private String injectTracking(
      String html, String campaignId, String pixelToken, String unsubToken) {
    String base = publicBase();
    String pixel = base + "/t/" + pixelToken;
    String unsub = base + "/unsubscribe/" + unsubToken;
    String body = html == null ? "" : html;
    if (!body.contains("/t/")) {
      body =
          body
              + "<img src=\""
              + pixel
              + "\" width=\"1\" height=\"1\" alt=\"\" style=\"display:none\"/>";
    }
    if (!body.contains("/unsubscribe/")) {
      body =
          body
              + "<p style=\"font-size:10px\"><a href=\""
              + unsub
              + "\">Unsubscribe</a></p>";
    }
    return body;
  }

  private String publicBase() {
    String base =
        parameters
            .get(ParameterKeys.PUBLIC_BASE_URL)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "public.base.url missing"));
    return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
  }

  private boolean verpEnabled() {
    return parameters
        .get(ParameterKeys.VERP_ENABLED)
        .map(v -> "true".equalsIgnoreCase(v.trim()))
        .orElse(false);
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
        && campaign.getStatus() != CampaignStatus.STOPPED
        && campaign.getStatus() != CampaignStatus.FINISHED) {
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
    if (campaign.getStatus() == CampaignStatus.FINISHED
        && linked.stream().allMatch(CampaignListEntity::isIncludedInPass)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Finished requeue requires an added list");
    }
    // first ordered list determines sending path (AD)
    ListEntity firstList =
        lists
            .findById(linked.get(0).getListId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "list missing"));
    String pathId = firstList.getSendingPathId();
    List<ListEntity> selectedLists = new ArrayList<>();
    for (CampaignListEntity cl : linked) {
      lists.findById(cl.getListId()).ifPresent(selectedLists::add);
    }
    ImageFetchMode mode =
        ImageFetchMode.from(parameters.get(ParameterKeys.IMAGE_FETCH_MODE).orElse("Off"));
    String rewrittenHtml =
        imageFetch.ingest(campaignId, campaign.getHtmlBody(), mode, selectedLists);
    campaign.setHtmlBody(rewrittenHtml);
    campaigns.save(campaign);

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
    int enqueued = 0;
    for (MemberEntity member : unique.values()) {
      String emailKey = member.getEmail().trim().toLowerCase(Locale.ROOT);
      boolean already =
          queue.findByCampaignId(campaignId).stream()
              .anyMatch(
                  r -> {
                    if (!"SENT".equals(r.getStatus()) && !"PENDING".equals(r.getStatus())) {
                      return false;
                    }
                    if (member.getId().equals(r.getMemberId())) {
                      return true;
                    }
                    return r.getEmail() != null
                        && emailKey.equals(r.getEmail().trim().toLowerCase(Locale.ROOT));
                  });
      if (already) {
        continue;
      }
      queue.save(
          new SendQueueEntity(
              Guids.newGuid(),
              campaignId,
              member.getId(),
              member.getEmail(),
              pathId,
              "PENDING"));
      enqueued++;
    }
    for (CampaignListEntity cl : linked) {
      cl.setIncludedInPass(true);
      campaignLists.save(cl);
    }
    boolean anyPending =
        queue.findByCampaignId(campaignId).stream()
            .anyMatch(r -> "PENDING".equals(r.getStatus()));
    if (!anyPending && enqueued == 0) {
      // Finished requeue with no new addresses — stay Finished, not Queued limbo
      campaign.setStatus(CampaignStatus.FINISHED);
    } else {
      campaign.setStatus(CampaignStatus.QUEUED);
    }
    return campaigns.save(campaign);
  }

  /**
   * Drain up to {@code maxSends} PENDING queue rows system-wide. Returns how many SMTP sends
   * completed. Idempotent for empty queue.
   */
  @Transactional
  public int processPendingBatch(int maxSends) {
    if (maxSends <= 0) {
      return 0;
    }
    List<SendQueueEntity> pending = queue.findByStatus("PENDING");
    // shortcut: empty ticks are not written
    if (pending == null || pending.isEmpty()) {
      return 0;
    }
    activity.write(ActivityLogService.SEND, "START", "queue send pending=" + pending.size());
    int sent = 0;
    String openPath = null;
    try {
      for (SendQueueEntity row : pending) {
        if (sent >= maxSends) {
          break;
        }
        CampaignEntity campaign = campaigns.findById(row.getCampaignId()).orElse(null);
        if (campaign == null
            || campaign.getStatus() == CampaignStatus.STOPPED
            || campaign.getStatus() == CampaignStatus.DRAFT) {
          continue;
        }
        if (campaign.getStatus() == CampaignStatus.QUEUED) {
          campaign.setStatus(CampaignStatus.SENDING);
          campaigns.save(campaign);
        }
        List<CampaignAttachmentEntity> blobs = attachments.findByCampaignId(campaign.getId());
        List<SmtpPort.Attachment> files =
            blobs.stream()
                .map(
                    a ->
                        new SmtpPort.Attachment(
                            a.getFilename(), a.getContentType(), a.getContent()))
                .toList();
        String pixelToken = Guids.newGuid();
        List<String> listSnapshot =
            campaignLists.findByCampaignIdOrderByPositionAscListIdAsc(campaign.getId()).stream()
                .map(CampaignListEntity::getListId)
                .toList();
        String unsubToken =
            opaqueTokens.seal(
                UnsubTokenCodec.encode(
                    new UnsubTokenCodec.Payload(
                        row.getMemberId(), campaign.getId(), listSnapshot, row.getId())));
        String corrToken = Guids.newGuid();
        row.setPixelToken(pixelToken);
        row.setUnsubToken(unsubToken);
        row.setCorrelationToken(corrToken);
        row.setSubject(campaign.getSubject());
        String smtpId = "<" + Guids.newGuid() + "@listmailer>";
        row.setSmtpMessageId(smtpId);
        if (verpEnabled()) {
          sendingPaths
              .findById(row.getSendingPathId())
              .ifPresent(
                  path -> {
                    String domain = path.getFromEmail();
                    int at = domain.lastIndexOf('@');
                    if (at > 0) {
                      row.setVerpRcpt("bounce+" + corrToken + domain.substring(at));
                    }
                  });
        }
        String html = rewriteHtml(campaign.getId(), campaign.getHtmlBody(), blobs);
        html = injectTracking(html, campaign.getId(), pixelToken, unsubToken);
        String unsubUrl = publicBase() + "/unsubscribe/" + unsubToken;
        java.util.Map<String, String> headers = new java.util.LinkedHashMap<>();
        headers.put("X-JavaList-Token", corrToken);
        headers.put("Message-ID", smtpId);
        if (openPath == null || !openPath.equals(row.getSendingPathId())) {
          if (openPath != null) {
            activity.write(ActivityLogService.SEND, "DISCONNECT", "path=" + pathLabel(openPath));
          }
          openPath = row.getSendingPathId();
          activity.write(ActivityLogService.SEND, "CONNECT", "path=" + pathLabel(openPath));
        }
        smtp.send(
            new SmtpPort.SmtpMessage(
                row.getSendingPathId(),
                row.getEmail(),
                campaign.getSubject(),
                campaign.getTextBody(),
                html,
                files,
                unsubUrl,
                headers));
        row.setStatus("SENT");
        row.setSentAt(java.time.Instant.now());
        queue.save(row);
        sent++;
        activity.write(
            ActivityLogService.SEND,
            "SEND",
            "to=" + row.getEmail() + " campaign=" + campaign.getId() + " subject=" + campaign.getSubject());
        boolean morePending =
            queue.findByCampaignId(campaign.getId()).stream()
                .anyMatch(r -> "PENDING".equals(r.getStatus()));
        if (!morePending && campaign.getStatus() == CampaignStatus.SENDING) {
          campaign.setStatus(CampaignStatus.FINISHED);
          campaigns.save(campaign);
          ops.audit("campaign.finished", "campaign=" + campaign.getId(), "system", null, null);
        }
      }
      return sent;
    } finally {
      if (openPath != null) {
        activity.write(ActivityLogService.SEND, "DISCONNECT", "path=" + pathLabel(openPath));
      }
      activity.write(ActivityLogService.SEND, "END", "queue send sent=" + sent);
    }
  }

  private String pathLabel(String pathId) {
    var found = sendingPaths.findById(pathId);
    if (found != null && found.isPresent()) {
      var path = found.get();
      return path.getName() + " host=" + path.getHost();
    }
    return pathId == null ? "" : pathId;
  }
}
