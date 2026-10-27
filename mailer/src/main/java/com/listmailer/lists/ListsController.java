package com.listmailer.lists;

import com.listmailer.campaigns.SendPauseCache;
import com.listmailer.catalogs.CatalogReferenceService;
import com.listmailer.catalogs.CatalogTargetType;
import com.listmailer.catalogs.FooterRepository;
import com.listmailer.catalogs.SendingPathRepository;
import com.listmailer.kernel.ids.Guids;
import com.listmailer.users.ListAccessService;
import com.listmailer.users.UserListGrantEntity;
import com.listmailer.users.UserListGrantRepository;
import com.listmailer.users.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/lists")
public class ListsController {

  private final ListRepository lists;
  private final SendingPathRepository paths;
  private final FooterRepository footers;
  private final CatalogReferenceService references;
  private final ListAccessService access;
  private final UserListGrantRepository grants;
  private final SendPauseCache pauseCache;

  public ListsController(
      ListRepository lists,
      SendingPathRepository paths,
      FooterRepository footers,
      CatalogReferenceService references,
      ListAccessService access,
      UserListGrantRepository grants,
      SendPauseCache pauseCache) {
    this.lists = lists;
    this.paths = paths;
    this.footers = footers;
    this.references = references;
    this.access = access;
    this.grants = grants;
    this.pauseCache = pauseCache;
  }

  @GetMapping
  public List<ListSummary> index() {
    if (access.isAdmin()) {
      return lists.findAll().stream().map(ListSummary::from).toList();
    }
    String userId = currentUserId();
    var ids =
        grants.findByUserId(userId).stream()
            .filter(g -> g.isCanManage() || g.isCanSend())
            .map(UserListGrantEntity::getListId)
            .toList();
    return lists.findByIdIn(ids).stream().map(ListSummary::from).toList();
  }

  @PostMapping
  @Transactional
  public ListDetail create(@RequestBody ListRequest body) {
    requireCreateRole();
    validate(body);
    String id = Guids.newGuid();
    ListEntity saved =
        lists.save(
            new ListEntity(
                id,
                body.name().trim(),
                body.sendingPathId(),
                blankToNull(body.footerId()),
                body.spamThreshold(),
                blankToNull(body.payloadOverride()),
                body.imageTtlOverrideHours(),
                Instant.now()));
    wireReferences(null, null, saved);
    if (!access.isAdmin()) {
      grants.save(new UserListGrantEntity(currentUserId(), id, true, true));
    }
    return ListDetail.from(saved, true, true);
  }

  @GetMapping("/{listId}")
  public ListDetail get(@PathVariable String listId) {
    ListEntity list = require(listId);
    if (!access.canAccess(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no grant for list");
    }
    return ListDetail.from(list, access.canManage(listId), access.canSend(listId));
  }

  @PutMapping("/{listId}")
  @Transactional
  public ListDetail update(@PathVariable String listId, @RequestBody ListRequest body) {
    if (!access.canManage(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
    }
    ListEntity list = require(listId);
    validate(body);
    String oldPath = list.getSendingPathId();
    String oldFooter = list.getFooterId();
    list.setName(body.name().trim());
    list.setSendingPathId(body.sendingPathId());
    list.setFooterId(blankToNull(body.footerId()));
    list.setSpamThreshold(body.spamThreshold());
    list.setPayloadOverride(blankToNull(body.payloadOverride()));
    list.setImageTtlOverrideHours(body.imageTtlOverrideHours());
    ListEntity saved = lists.save(list);
    wireReferences(oldPath, oldFooter, saved);
    return ListDetail.from(saved, true, access.canSend(listId));
  }

  @DeleteMapping("/{listId}")
  @Transactional
  public void delete(@PathVariable String listId) {
    if (!access.canManage(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
    }
    ListEntity list = require(listId);
    references.remove(CatalogTargetType.SENDING_PATH, list.getSendingPathId(), "LIST", listId);
    if (list.getFooterId() != null) {
      references.remove(CatalogTargetType.FOOTER, list.getFooterId(), "LIST", listId);
    }
    grants.deleteByListId(listId);
    lists.delete(list);
  }

  @PostMapping("/{listId}/pause")
  @Transactional
  public ListDetail pause(@PathVariable String listId) {
    if (!access.canManage(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
    }
    ListEntity list = require(listId);
    list.setPaused(true);
    ListDetail out = ListDetail.from(lists.save(list), true, access.canSend(listId));
    pauseCache.clearList(listId);
    return out;
  }

  @PostMapping("/{listId}/resume")
  @Transactional
  public ListDetail resume(@PathVariable String listId) {
    if (!access.canManage(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
    }
    ListEntity list = require(listId);
    list.setPaused(false);
    ListDetail out = ListDetail.from(lists.save(list), true, access.canSend(listId));
    pauseCache.clearList(listId);
    return out;
  }

  private void wireReferences(String oldPath, String oldFooter, ListEntity list) {
    if (oldPath != null && !oldPath.equals(list.getSendingPathId())) {
      references.remove(CatalogTargetType.SENDING_PATH, oldPath, "LIST", list.getId());
    }
    if (oldFooter != null && (list.getFooterId() == null || !oldFooter.equals(list.getFooterId()))) {
      references.remove(CatalogTargetType.FOOTER, oldFooter, "LIST", list.getId());
    }
    references.add(
        CatalogTargetType.SENDING_PATH,
        list.getSendingPathId(),
        "LIST",
        list.getId(),
        list.getName());
    if (list.getFooterId() != null) {
      references.add(
          CatalogTargetType.FOOTER, list.getFooterId(), "LIST", list.getId(), list.getName());
    }
  }

  private void validate(ListRequest body) {
    if (body == null || !StringUtils.hasText(body.name()) || !StringUtils.hasText(body.sendingPathId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name and sendingPathId required");
    }
    if (!paths.existsById(body.sendingPathId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown sending path");
    }
    if (StringUtils.hasText(body.footerId()) && !footers.existsById(body.footerId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unknown footer");
    }
  }

  private void requireCreateRole() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean allowed =
        auth.getAuthorities().stream()
            .anyMatch(
                a ->
                    a.getAuthority().equals("ROLE_" + UserRole.ADMIN.name())
                        || a.getAuthority().equals("ROLE_" + UserRole.LIST_MANAGER.name()));
    if (!allowed) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin or List manager required");
    }
  }

  private ListEntity require(String id) {
    return lists
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "list not found"));
  }

  private String currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
      return jwt.getSubject();
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
  }

  private static String blankToNull(String value) {
    return StringUtils.hasText(value) ? value : null;
  }

  public record ListRequest(
      String name,
      String sendingPathId,
      String footerId,
      BigDecimal spamThreshold,
      String payloadOverride,
      Integer imageTtlOverrideHours) {}

  public record ListSummary(String id, String name, String sendingPathId) {
    static ListSummary from(ListEntity list) {
      return new ListSummary(list.getId(), list.getName(), list.getSendingPathId());
    }
  }

  public record ListDetail(
      String id,
      String name,
      String sendingPathId,
      String footerId,
      BigDecimal spamThreshold,
      String payloadOverride,
      Integer imageTtlOverrideHours,
      boolean canManage,
      boolean canSend,
      boolean paused) {
    static ListDetail from(ListEntity list, boolean canManage, boolean canSend) {
      return new ListDetail(
          list.getId(),
          list.getName(),
          list.getSendingPathId(),
          list.getFooterId() == null ? "" : list.getFooterId(),
          list.getSpamThreshold(),
          list.getPayloadOverride() == null ? "" : list.getPayloadOverride(),
          list.getImageTtlOverrideHours(),
          canManage,
          canSend,
          list.isPaused());
    }
  }
}
