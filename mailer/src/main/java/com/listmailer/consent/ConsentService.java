package com.listmailer.consent;

import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.kernel.token.OpaqueTokenHelper;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import com.listmailer.noticemail.NoticeMailService;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ConsentService {

  public enum ApproveOutcome {
    CLEARED,
    ALREADY_CLEAR,
    EXPIRED,
    INVALID
  }

  private static final long DEFAULT_DAYS = 14;

  private final OpaqueTokenHelper tokens;
  private final MemberRepository members;
  private final ListMemberRepository listMembers;
  private final ListAccessService access;
  private final NoticeMailService noticeMail;
  private final ParametersPort parameters;
  private final OpsHooksPort ops;

  public ConsentService(
      OpaqueTokenHelper tokens,
      MemberRepository members,
      ListMemberRepository listMembers,
      ListAccessService access,
      NoticeMailService noticeMail,
      ParametersPort parameters,
      OpsHooksPort ops) {
    this.tokens = tokens;
    this.members = members;
    this.listMembers = listMembers;
    this.access = access;
    this.noticeMail = noticeMail;
    this.parameters = parameters;
    this.ops = ops;
  }

  /** Operator starts consent; never clears unsub itself. */
  @Transactional(readOnly = true)
  public void requestClearUnsub(String memberId, List<String> requestedListIds) {
    MemberEntity member =
        members
            .findById(memberId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
    List<String> listIds = resolveUnsubLists(memberId, requestedListIds);
    if (listIds.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no unsubscribed lists in scope");
    }
    for (String listId : listIds) {
      if (!access.isAdmin() && !access.canManage(listId)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
      }
    }
    long expires =
        Instant.now().toEpochMilli() + TimeUnit.DAYS.toMillis(expiryDays());
    String token =
        tokens.seal(
            ConsentTokenCodec.encode(new ConsentTokenCodec.Payload(memberId, listIds, expires)));
    String base =
        parameters
            .get(ParameterKeys.PUBLIC_BASE_URL)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "public.base.url missing"));
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    String url = base + "/approve/" + token;
    String subject = "Confirm re-subscribe to mailing lists";
    String text =
        "To clear your unsubscribe and receive mail again, open this link within "
            + expiryDays()
            + " days:\n"
            + url
            + "\n";
    String html =
        "<p>To clear your unsubscribe and receive mail again, open this link within "
            + expiryDays()
            + " days:</p><p><a href=\""
            + url
            + "\">Confirm</a></p>";
    // R3: notice path still delivers when member is bounce-disabled
    noticeMail.send(member.getEmail(), subject, text, html);
    ops.audit(
        "consent.request",
        "member=" + memberId + " lists=" + listIds.size() + " enabled=" + member.isEnabled());
  }

  @Transactional
  public ApproveOutcome approve(String rawToken) {
    if (!StringUtils.hasText(rawToken)) {
      return ApproveOutcome.INVALID;
    }
    ConsentTokenCodec.Payload payload;
    try {
      payload = ConsentTokenCodec.decode(tokens.open(rawToken.trim()));
    } catch (RuntimeException ex) {
      return ApproveOutcome.INVALID;
    }
    if (Instant.now().toEpochMilli() > payload.expiresEpochMs()) {
      return ApproveOutcome.EXPIRED;
    }
    if (members.findById(payload.memberId()).isEmpty()) {
      return ApproveOutcome.INVALID;
    }
    int cleared = 0;
    int already = 0;
    for (String listId : payload.listIds()) {
      var row = listMembers.findByListIdAndMemberId(listId, payload.memberId());
      if (row.isEmpty()) {
        continue;
      }
      ListMemberEntity lm = row.get();
      if (!lm.isUnsubscribed()) {
        already++;
        continue;
      }
      lm.setUnsubscribed(false);
      listMembers.save(lm);
      cleared++;
    }
    if (cleared > 0) {
      ops.audit(
          "consent.approve",
          "member=" + payload.memberId() + " listsCleared=" + cleared);
      return ApproveOutcome.CLEARED;
    }
    if (already > 0) {
      return ApproveOutcome.ALREADY_CLEAR;
    }
    return ApproveOutcome.INVALID;
  }

  private List<String> resolveUnsubLists(String memberId, List<String> requested) {
    List<String> out = new ArrayList<>();
    for (ListMemberEntity lm : listMembers.findByMemberId(memberId)) {
      if (!lm.isUnsubscribed()) {
        continue;
      }
      if (requested != null && !requested.isEmpty() && !requested.contains(lm.getListId())) {
        continue;
      }
      out.add(lm.getListId());
    }
    return out;
  }

  private long expiryDays() {
    return parameters
        .get("consent.approve.expiry.days")
        .map(
            v -> {
              try {
                long n = Long.parseLong(v.trim());
                return n > 0 ? n : DEFAULT_DAYS;
              } catch (NumberFormatException e) {
                return DEFAULT_DAYS;
              }
            })
        .orElse(DEFAULT_DAYS);
  }
}
