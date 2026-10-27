package com.listmailer.mts;

import com.listmailer.catalogs.SendingPathEntity;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.noticemail.NoticeMailService;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MtsFromDomainService {

  private final MtsFromDomainIssueRepository issues;
  private final ListRepository lists;
  private final SendingPathRepository paths;
  private final NoticeMailService noticeMail;
  private final OpsHooksPort ops;

  public MtsFromDomainService(
      MtsFromDomainIssueRepository issues,
      ListRepository lists,
      SendingPathRepository paths,
      NoticeMailService noticeMail,
      OpsHooksPort ops) {
    this.issues = issues;
    this.lists = lists;
    this.paths = paths;
    this.noticeMail = noticeMail;
    this.ops = ops;
  }

  /** Returns null if OK; otherwise opens Issue (no Campaign). All-or-nothing. */
  @Transactional
  public MtsFromDomainIssueEntity validateOrOpenIssue(
      MtsInboxEntity inbox,
      MtsAuthorizedSenderEntity sender,
      List<MtsSenderListGrantEntity> grants,
      String subject) {
    String sub = sender == null ? null : sender.getSubstituteFrom();
    if (!StringUtils.hasText(sub)) {
      return null;
    }
    String subDomain = domainOf(sub);
    String senderEmail = sender.getEmail();
    if (StringUtils.hasText(sender.getSendingPathId())) {
      SendingPathEntity path =
          paths
              .findById(sender.getSendingPathId())
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "path missing"));
      String pathDomain = domainOf(path.getFromEmail());
      if (!subDomain.equalsIgnoreCase(pathDomain)) {
        String grantId = grants.isEmpty() ? null : grants.get(0).getId();
        return openIssue(
            inbox,
            grantId,
            senderEmail,
            subject,
            "substitute From domain "
                + subDomain
                + " != sender path "
                + pathDomain);
      }
      return null;
    }
    for (MtsSenderListGrantEntity grant : grants) {
      ListEntity list =
          lists
              .findById(grant.getListId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "list missing"));
      SendingPathEntity path =
          paths
              .findById(list.getSendingPathId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "path missing"));
      String pathDomain = domainOf(path.getFromEmail());
      if (!subDomain.equalsIgnoreCase(pathDomain)) {
        return openIssue(
            inbox,
            grant.getId(),
            senderEmail,
            subject,
            "substitute From domain "
                + subDomain
                + " != path "
                + pathDomain
                + " for list "
                + grant.getListId());
      }
    }
    return null;
  }

  @Transactional
  public MtsFromDomainIssueEntity openIssue(
      MtsInboxEntity inbox,
      String grantId,
      String senderEmail,
      String subject,
      String detail) {
    // Prefer hold; V1 IMAP CREATE not wired — delete-and-notify fallback (R2.6)
    String resolution = "DELETED_NOTIFY";
    MtsFromDomainIssueEntity issue =
        issues.save(
            new MtsFromDomainIssueEntity(
                Guids.newGuid(),
                inbox.getId(),
                grantId,
                senderEmail,
                subject,
                "OPEN",
                resolution,
                detail,
                Instant.now()));
    try {
      noticeMail.send(
          senderEmail,
          "Mail-to-send: message deleted (From-domain)",
          "Your message could not be held; it was deleted. Please fix the From domain and resend.\n"
              + detail,
          "<p>Your message could not be held; it was <strong>deleted</strong>. Fix the From domain and resend.</p><p>"
              + detail
              + "</p>");
    } catch (RuntimeException ex) {
      ops.opsLog("mts.from_domain", "notify failed: " + ex.getMessage());
    }
    ops.opsLog("mts.from_domain", "issue=" + issue.getId() + " " + resolution);
    ops.attentionAdmin("From-domain Issue", detail);
    return issue;
  }

  @Transactional
  public void clear(String issueId) {
    MtsFromDomainIssueEntity issue =
        issues
            .findById(issueId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    issue.setStatus("CLEARED");
    issues.save(issue);
  }

  public List<MtsFromDomainIssueEntity> openIssues() {
    return issues.findByStatus("OPEN");
  }

  /** Re-validate mappings after Sending-path From change (R2.3). */
  @Transactional
  public int revalidateForSendingPath(String pathId) {
    int n = 0;
    String pathDomain =
        paths.findById(pathId).map(p -> domainOf(p.getFromEmail())).orElse("");
    if (!StringUtils.hasText(pathDomain)) {
      return 0;
    }
    for (ListEntity list : lists.findAll()) {
      if (!pathId.equals(list.getSendingPathId())) {
        continue;
      }
      ops.opsLog(
          "mts.from_domain",
          "path " + pathId + " changed; rematch on next ingest for list " + list.getId());
      n++;
    }
    return n;
  }

  public static String domainOf(String email) {
    return MtsEmails.domain(email);
  }
}
