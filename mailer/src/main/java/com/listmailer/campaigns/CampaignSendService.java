package com.listmailer.campaigns;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.kernel.ports.SmtpPort;
import com.listmailer.kernel.token.OpaqueTokenHelper;
import com.listmailer.noticemail.AdminNoticeMail;
import com.listmailer.ops.ActivityLogService;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import com.listmailer.unsubscribe.UnsubTokenCodec;
import java.time.Duration;
import java.time.Instant;
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
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
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
  private final RemoteImageFetchService imageFetch;
  private final OpaqueTokenHelper opaqueTokens;
  private final SendingPathRepository sendingPaths;
  private final ActivityLogService activity;
  private final TransactionTemplate transactions;
  private final SendPauseCache pauseCache;
  private final AdminNoticeMail adminNotices;

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
      RemoteImageFetchService imageFetch,
      OpaqueTokenHelper opaqueTokens,
      SendingPathRepository sendingPaths,
      ActivityLogService activity,
      TransactionTemplate transactions,
      SendPauseCache pauseCache,
      AdminNoticeMail adminNotices) {
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
    this.imageFetch = imageFetch;
    this.opaqueTokens = opaqueTokens;
    this.sendingPaths = sendingPaths;
    this.activity = activity;
    this.transactions = transactions;
    this.pauseCache = pauseCache;
    this.adminNotices = adminNotices;
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
    String pathId = resolveSendingPathId(campaign, firstList);
    InlineImagePlanner.Plan plan = planOutbound(campaign);
    int sent = 0;
    for (String to : addresses) {
      try {
        smtp.send(
            new SmtpPort.SmtpMessage(
                pathId,
                to,
                "[TEST] " + campaign.getSubject(),
                campaign.getTextBody(),
                plan.html(),
                mergeParts(plan)));
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

  private InlineImagePlanner.Plan planOutbound(CampaignEntity campaign) {
    List<CampaignAttachmentEntity> blobs = attachments.findByCampaignId(campaign.getId());
    int perKb =
        intParam(ParameterKeys.CAMPAIGN_IMAGE_INLINE_MAX_KB, 256);
    int totalKb =
        intParam(ParameterKeys.CAMPAIGN_IMAGE_INLINE_TOTAL_MAX_KB, 2048);
    return InlineImagePlanner.plan(
        campaign.isEmbedImages(),
        campaign.getId(),
        publicBase(),
        campaign.getHtmlBody(),
        blobs,
        perKb,
        totalKb);
  }

  private static List<SmtpPort.Attachment> mergeParts(InlineImagePlanner.Plan plan) {
    List<SmtpPort.Attachment> all = new ArrayList<>();
    all.addAll(plan.inlineParts());
    all.addAll(plan.fileParts());
    return all;
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
    // campaign path overrides first ordered list path when set
    ListEntity firstList =
        lists
            .findById(linked.get(0).getListId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "list missing"));
    String pathId = resolveSendingPathId(campaign, firstList);
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

    // address-level dedupe (R1): same email → one queue row; first list wins for list_id
    Map<String, MemberEntity> unique = new LinkedHashMap<>();
    Map<String, String> emailToList = new LinkedHashMap<>();
    for (CampaignListEntity cl : linked) {
      for (ListMemberEntity lm : listMembers.findByListId(cl.getListId())) {
        if (lm.isUnsubscribed()) {
          continue;
        }
        members
            .findById(lm.getMemberId())
            .filter(MemberEntity::isEnabled)
            .ifPresent(
                m -> {
                  String key = m.getEmail().trim().toLowerCase(Locale.ROOT);
                  if (unique.putIfAbsent(key, m) == null) {
                    emailToList.put(key, cl.getListId());
                  }
                });
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
      SendQueueEntity row =
          new SendQueueEntity(
              Guids.newGuid(),
              campaignId,
              member.getId(),
              member.getEmail(),
              pathId,
              "PENDING");
      row.setListId(emailToList.get(emailKey));
      queue.save(row);
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
      campaign.setQueuedAt(Instant.now());
    }
    CampaignEntity saved = campaigns.save(campaign);
    if (saved.getStatus() == CampaignStatus.QUEUED) {
      notifyCampaignQueued(saved);
    }
    return saved;
  }

  private void notifyCampaignQueued(CampaignEntity campaign) {
    try {
      String subject = "Campaign Queued: " + (campaign.getSubject() == null ? "" : campaign.getSubject());
      String text =
          StringUtils.hasText(campaign.getTextBody())
              ? campaign.getTextBody()
              : "(no text body)";
      String html =
          StringUtils.hasText(campaign.getHtmlBody())
              ? campaign.getHtmlBody()
              : "<p>(no HTML body)</p>";
      adminNotices.send(subject, text, html);
    } catch (RuntimeException ignored) {
      // never fail queue for notice mail
    }
  }

  /** Drain up to {@code maxSends} PENDING rows. Empty queue writes nothing. */
  public int processPendingBatch(int maxSends) {
    return processPendingBatch(maxSends, 0L, null);
  }

  /**
   * Each SMTP send commits on its own. {@code delayMs} is slept between sends, outside the
   * transaction. A non-null {@code deadline} stops the run before the next send.
   */
  public int processPendingBatch(int maxSends, long delayMs, java.time.Instant deadline) {
    if (maxSends <= 0) {
      return 0;
    }
    if (pauseCache.globalPaused()) {
      return 0;
    }
    java.time.Instant now = java.time.Instant.now();
    List<SendQueueEntity> pending = new ArrayList<>();
    List<SendQueueEntity> duePending = queue.findByStatus("PENDING");
    if (duePending != null) {
      pending.addAll(duePending);
    }
    List<SendQueueEntity> retries = queue.findByStatus("FAILED_RETRY_PENDING");
    if (retries != null) {
      for (SendQueueEntity row : retries) {
        if (row.getNextAttemptAt() == null || !row.getNextAttemptAt().isAfter(now)) {
          pending.add(row);
        }
      }
    }
    // shortcut: empty ticks are not written
    if (pending.isEmpty()) {
      return 0;
    }
    Instant startedAt = Instant.now();
    activity.write(ActivityLogService.SEND, "START", "queue send pending=" + pending.size());
    int sent = 0;
    int tries = 0;
    String openPath = null;
    Map<String, int[]> perCampaign = new LinkedHashMap<>(); // id -> [sent, errored]
    try {
      for (SendQueueEntity row : pending) {
        if (tries >= maxSends) {
          break;
        }
        if (deadline != null && !Instant.now().isBefore(deadline)) {
          break;
        }
        if (pauseCache.globalPaused()
            || pauseCache.campaignPaused(row.getCampaignId())
            || pauseCache.listPaused(row.getListId())) {
          continue;
        }
        CampaignEntity peek = campaigns.findById(row.getCampaignId()).orElse(null);
        if (peek == null
            || peek.getStatus() == CampaignStatus.STOPPED
            || peek.getStatus() == CampaignStatus.DRAFT
            || peek.getStatus() == CampaignStatus.PAUSED) {
          continue;
        }
        if (tries > 0 && delayMs > 0) {
          long wait = delayMs;
          if (deadline != null) {
            long remain = Duration.between(Instant.now(), deadline).toMillis();
            if (remain <= 0) {
              break;
            }
            wait = Math.min(delayMs, remain);
          }
          try {
            Thread.sleep(wait);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            break;
          }
          if (deadline != null && !Instant.now().isBefore(deadline)) {
            break;
          }
        }
        if (openPath == null || !openPath.equals(row.getSendingPathId())) {
          if (openPath != null) {
            activity.write(ActivityLogService.SEND, "DISCONNECT", "path=" + pathLabel(openPath));
          }
          openPath = row.getSendingPathId();
          activity.write(ActivityLogService.SEND, "CONNECT", "path=" + pathLabel(openPath));
        }
        try {
          if (!Boolean.TRUE.equals(inTx(status -> deliver(row)))) {
            continue;
          }
          tries++;
          sent++;
          bump(perCampaign, row.getCampaignId(), true);
        } catch (RuntimeException ex) {
          tries++;
          bump(perCampaign, row.getCampaignId(), false);
          try {
            inTx(
                status -> {
                  recordFailure(row.getId());
                  return null;
                });
          } catch (RuntimeException ignored) {
            // keep draining other recipients
          }
          activity.write(
              ActivityLogService.SEND,
              "FAIL",
              "to="
                  + row.getEmail()
                  + " campaign="
                  + row.getCampaignId()
                  + " "
                  + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage()));
        }
      }
      return sent;
    } finally {
      if (openPath != null) {
        activity.write(ActivityLogService.SEND, "DISCONNECT", "path=" + pathLabel(openPath));
      }
      activity.write(ActivityLogService.SEND, "END", "queue send sent=" + sent);
      notifyQueueProcessed(startedAt, Instant.now(), sent, perCampaign);
    }
  }

  private static void bump(Map<String, int[]> perCampaign, String campaignId, boolean ok) {
    int[] n = perCampaign.computeIfAbsent(campaignId, id -> new int[2]);
    if (ok) {
      n[0]++;
    } else {
      n[1]++;
    }
  }

  private void notifyQueueProcessed(
      Instant startedAt, Instant finishedAt, int sent, Map<String, int[]> perCampaign) {
    try {
      long millis = Math.max(1L, Duration.between(startedAt, finishedAt).toMillis());
      double messagesPerHour = sent * 3_600_000.0 / millis;
      StringBuilder text = new StringBuilder();
      text.append("Start: ").append(startedAt).append('\n');
      text.append("Finish: ").append(finishedAt).append('\n');
      text.append(String.format(Locale.ROOT, "Messages per hour: %.2f%n", messagesPerHour));
      text.append('\n');
      StringBuilder html = new StringBuilder();
      html.append("<p><strong>Start:</strong> ").append(startedAt).append("</p>");
      html.append("<p><strong>Finish:</strong> ").append(finishedAt).append("</p>");
      html.append(
          String.format(Locale.ROOT, "<p><strong>Messages per hour:</strong> %.2f</p>", messagesPerHour));
      html.append("<table><tr><th>Campaign</th><th>Sent</th><th>Errored</th></tr>");
      if (perCampaign.isEmpty()) {
        text.append("(No sends or errors in this run — pending work was skipped.)\n");
        html.append("<tr><td colspan=\"3\">No sends or errors (pending skipped)</td></tr>");
      } else {
        for (Map.Entry<String, int[]> e : perCampaign.entrySet()) {
          String label =
              campaigns
                  .findById(e.getKey())
                  .map(c -> c.getSubject() + " (" + e.getKey() + ")")
                  .orElse(e.getKey());
          int s = e.getValue()[0];
          int err = e.getValue()[1];
          text.append(label)
              .append(": sent=")
              .append(s)
              .append(" errored=")
              .append(err)
              .append('\n');
          html.append("<tr><td>")
              .append(label)
              .append("</td><td>")
              .append(s)
              .append("</td><td>")
              .append(err)
              .append("</td></tr>");
        }
      }
      html.append("</table>");
      adminNotices.send("Queue Processed", text.toString(), html.toString());
    } catch (RuntimeException ex) {
      // never fail the queue drain for notice mail
    }
  }

  @Transactional
  public CampaignEntity retryFailed(String campaignId) {
    CampaignEntity campaign =
        campaigns
            .findById(campaignId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
    boolean any = false;
    for (SendQueueEntity row : queue.findByCampaignId(campaignId)) {
      if (!"SEND_FAILED".equals(row.getStatus())) {
        continue;
      }
      row.setStatus("PENDING");
      row.setRetryAttempts(0);
      row.setNextAttemptAt(null);
      queue.save(row);
      any = true;
    }
    if (!any) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "no Send Failed rows");
    }
    if (campaign.getStatus() == CampaignStatus.FINISHED) {
      campaign.setStatus(CampaignStatus.QUEUED);
      campaign.setQueuedAt(java.time.Instant.now());
      campaigns.save(campaign);
    }
    return campaign;
  }

  private void recordFailure(String queueId) {
    SendQueueEntity row = queue.findById(queueId).orElse(null);
    if (row == null) {
      return;
    }
    String status = row.getStatus();
    if (!"PENDING".equals(status) && !"FAILED_RETRY_PENDING".equals(status)) {
      return;
    }
    int maxFurther = intParam(ParameterKeys.SEND_RETRY_COUNT, 3);
    int delayMin = intParam(ParameterKeys.SEND_RETRY_DELAY_MINUTES, 5);
    if ("PENDING".equals(status)) {
      if (maxFurther <= 0) {
        row.setStatus("SEND_FAILED");
        row.setRetryAttempts(0);
        row.setNextAttemptAt(null);
      } else {
        row.setStatus("FAILED_RETRY_PENDING");
        row.setRetryAttempts(0);
        row.setNextAttemptAt(
            java.time.Instant.now().plus(delayMin, java.time.temporal.ChronoUnit.MINUTES));
      }
      queue.save(row);
      return;
    }
    int next = row.getRetryAttempts() + 1;
    row.setRetryAttempts(next);
    if (next >= maxFurther) {
      row.setStatus("SEND_FAILED");
      row.setNextAttemptAt(null);
    } else {
      row.setStatus("FAILED_RETRY_PENDING");
      row.setNextAttemptAt(java.time.Instant.now().plus(delayMin, java.time.temporal.ChronoUnit.MINUTES));
    }
    queue.save(row);
  }

  private int intParam(String key, int fallback) {
    return parameters
        .get(key)
        .map(
            v -> {
              try {
                int n = Integer.parseInt(v.trim());
                return n >= 0 ? n : fallback;
              } catch (NumberFormatException e) {
                return fallback;
              }
            })
        .orElse(fallback);
  }

  /** One message. Returns false when the campaign must be skipped. */
  private boolean deliver(SendQueueEntity row) {
    if (pauseCache.globalPaused()
        || pauseCache.campaignPaused(row.getCampaignId())
        || pauseCache.listPaused(row.getListId())) {
      return false;
    }
    CampaignEntity campaign = campaigns.findById(row.getCampaignId()).orElse(null);
    if (campaign == null
        || campaign.getStatus() == CampaignStatus.STOPPED
        || campaign.getStatus() == CampaignStatus.DRAFT
        || campaign.getStatus() == CampaignStatus.PAUSED) {
      return false;
    }
    if (campaign.getStatus() == CampaignStatus.QUEUED) {
      campaign.setStatus(CampaignStatus.SENDING);
      campaigns.save(campaign);
    }
    InlineImagePlanner.Plan plan = planOutbound(campaign);
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
                String seed =
                    StringUtils.hasText(path.getReturnPathEmail())
                        ? path.getReturnPathEmail()
                        : path.getFromEmail();
                int at = seed == null ? -1 : seed.lastIndexOf('@');
                if (at > 0) {
                  row.setVerpRcpt("bounce+" + corrToken + seed.substring(at));
                }
              });
    }
    String html = injectTracking(plan.html(), campaign.getId(), pixelToken, unsubToken);
    String unsubUrl = publicBase() + "/unsubscribe/" + unsubToken;
    java.util.Map<String, String> headers = new java.util.LinkedHashMap<>();
    headers.put("X-JavaList-Token", corrToken);
    headers.put("Message-ID", smtpId);
    smtp.send(
        new SmtpPort.SmtpMessage(
            row.getSendingPathId(),
            row.getEmail(),
            campaign.getSubject(),
            campaign.getTextBody(),
            html,
            mergeParts(plan),
            unsubUrl,
            headers));
    row.setStatus("SENT");
    row.setSentAt(java.time.Instant.now());
    row.setRetryAttempts(0);
    row.setNextAttemptAt(null);
    queue.save(row);
    activity.write(
        ActivityLogService.SEND,
        "SEND",
        "to=" + row.getEmail() + " campaign=" + campaign.getId() + " subject=" + campaign.getSubject());
    boolean moreOpen =
        queue.findByCampaignId(campaign.getId()).stream()
            .anyMatch(
                r ->
                    "PENDING".equals(r.getStatus())
                        || "FAILED_RETRY_PENDING".equals(r.getStatus()));
    if (!moreOpen && campaign.getStatus() == CampaignStatus.SENDING) {
      campaign.setStatus(CampaignStatus.FINISHED);
      campaigns.save(campaign);
      ops.audit("campaign.finished", "campaign=" + campaign.getId(), "system", null, null);
    }
    return true;
  }

  private <T> T inTx(TransactionCallback<T> callback) {
    if (transactions == null) {
      try {
        return callback.doInTransaction(
            new org.springframework.transaction.support.SimpleTransactionStatus());
      } catch (RuntimeException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new IllegalStateException(ex);
      }
    }
    return transactions.execute(callback);
  }

  private String pathLabel(String pathId) {
    var found = sendingPaths.findById(pathId);
    if (found != null && found.isPresent()) {
      var path = found.get();
      return path.getName() + " host=" + path.getHost();
    }
    return pathId == null ? "" : pathId;
  }

  private String resolveSendingPathId(CampaignEntity campaign, ListEntity firstList) {
    if (StringUtils.hasText(campaign.getSendingPathId())) {
      String id = campaign.getSendingPathId().trim();
      if (!sendingPaths.existsById(id)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown campaign sending path");
      }
      return id;
    }
    return firstList.getSendingPathId();
  }
}
