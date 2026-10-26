package com.listmailer.members;

import com.listmailer.consent.ConsentService;
import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import com.listmailer.users.UserListGrantEntity;
import com.listmailer.users.UserListGrantRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/members")
public class MembersController {

  private final MemberRepository members;
  private final ListMemberRepository listMembers;
  private final ListRepository lists;
  private final ListAccessService access;
  private final UserListGrantRepository grants;
  private final ConsentService consent;

  public MembersController(
      MemberRepository members,
      ListMemberRepository listMembers,
      ListRepository lists,
      ListAccessService access,
      UserListGrantRepository grants,
      ConsentService consent) {
    this.members = members;
    this.listMembers = listMembers;
    this.lists = lists;
    this.access = access;
    this.grants = grants;
    this.consent = consent;
  }

  @GetMapping
  public List<MemberSummary> index(@RequestParam(required = false) String q) {
    List<MemberEntity> all =
        StringUtils.hasText(q)
            ? members.findByEmailContainingIgnoreCase(q.trim())
            : members.findAll();
    if (access.isAdmin()) {
      return all.stream().map(MemberSummary::from).toList();
    }
    Set<String> allowedMemberIds = scopedMemberIds();
    return all.stream()
        .filter(m -> allowedMemberIds.contains(m.getId()))
        .map(MemberSummary::from)
        .toList();
  }

  @GetMapping("/{id}")
  public MemberDetail get(@PathVariable String id) {
    MemberEntity member = requireVisible(id);
    List<Membership> memberships = new ArrayList<>();
    for (ListMemberEntity lm : listMembers.findByMemberId(id)) {
      if (!access.isAdmin() && !access.canAccess(lm.getListId())) {
        continue;
      }
      String listName =
          lists.findById(lm.getListId()).map(ListEntity::getName).orElse(lm.getListId());
      memberships.add(new Membership(lm.getListId(), listName, lm.isUnsubscribed()));
    }
    return new MemberDetail(
        member.getId(),
        member.getEmail(),
        member.isEnabled(),
        member.getDisableReason() == null ? "" : member.getDisableReason(),
        memberships,
        List.of()); // timeline = audit filter; wired in ops epic
  }

  @PutMapping("/{id}")
  @Transactional
  public MemberDetail patch(@PathVariable String id, @RequestBody PatchBody body) {
    MemberEntity member = requireVisible(id);
    requireManageAnyListOf(member.getId());
    if (body.enabled() != null) {
      if (!body.enabled()) {
        if (!StringUtils.hasText(body.disableReason())) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "disableReason required");
        }
        member.setEnabled(false);
        member.setDisableReason(body.disableReason().trim());
      } else {
        // re-enable requires confirm flag (story 4.5)
        if (!Boolean.TRUE.equals(body.confirm())) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confirm=true required to re-enable");
        }
        member.setEnabled(true);
        member.setDisableReason(null);
      }
      members.save(member);
    }
    return get(id);
  }

  /** Starts consent clear-unsub via Notice path; does not clear unsub unilaterally (R2). */
  @PostMapping("/{id}/consent-clear-unsub")
  public Map<String, String> requestConsentClear(
      @PathVariable String id, @RequestBody(required = false) ConsentClearBody body) {
    requireVisible(id);
    consent.requestClearUnsub(id, body == null ? null : body.listIds());
    return Map.of("status", "consent_sent");
  }

  @PostMapping("/{id}/lists/{listId}")
  @Transactional
  public MemberDetail reAdd(
      @PathVariable String id, @PathVariable String listId, @RequestBody ReAddBody body) {
    MemberEntity member = requireVisible(id);
    if (!access.canManage(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
    }
    if (!Boolean.TRUE.equals(body == null ? null : body.confirm())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "confirm=true required");
    }
    var existing = listMembers.findByListIdAndMemberId(listId, id);
    if (existing.isPresent() && existing.get().isUnsubscribed()) {
      // R2: unsubscribed re-add ignored — clear only via consent /approve
      return get(id);
    }
    if (existing.isEmpty()) {
      listMembers.save(
          new ListMemberEntity(listId, id, false, java.time.Instant.now()));
    }
    if (!member.isEnabled()) {
      member.setEnabled(true);
      member.setDisableReason(null);
      members.save(member);
    }
    return get(id);
  }

  private MemberEntity requireVisible(String id) {
    MemberEntity member =
        members
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "not found"));
    if (!access.isAdmin() && !scopedMemberIds().contains(id)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "out of scope");
    }
    return member;
  }

  private void requireManageAnyListOf(String memberId) {
    if (access.isAdmin()) {
      return;
    }
    boolean ok =
        listMembers.findByMemberId(memberId).stream().anyMatch(lm -> access.canManage(lm.getListId()));
    if (!ok) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
    }
  }

  private Set<String> scopedMemberIds() {
    String userId = currentUserId();
    List<String> listIds =
        grants.findByUserId(userId).stream()
            .filter(g -> g.isCanManage() || g.isCanSend())
            .map(UserListGrantEntity::getListId)
            .toList();
    Set<String> ids = new LinkedHashSet<>();
    for (ListMemberEntity lm : listMembers.findByListIdIn(listIds)) {
      ids.add(lm.getMemberId());
    }
    return ids;
  }

  private String currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      return jwt.getSubject();
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  }

  public record MemberSummary(String id, String email, boolean enabled) {
    static MemberSummary from(MemberEntity m) {
      return new MemberSummary(m.getId(), m.getEmail(), m.isEnabled());
    }
  }

  public record Membership(String listId, String listName, boolean unsubscribed) {}

  public record MemberDetail(
      String id,
      String email,
      boolean enabled,
      String disableReason,
      List<Membership> lists,
      List<Object> timeline) {}

  public record PatchBody(Boolean enabled, String disableReason, Boolean confirm) {}

  public record ReAddBody(Boolean confirm) {}

  public record ConsentClearBody(List<String> listIds) {}
}
