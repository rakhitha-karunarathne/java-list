package com.listmailer.members;

import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import com.listmailer.users.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/lists/{listId}/members")
public class ListMembersController {

  private final ListRepository lists;
  private final ListAccessService access;
  private final BulkAddMembersService bulkAdd;

  public ListMembersController(
      ListRepository lists, ListAccessService access, BulkAddMembersService bulkAdd) {
    this.lists = lists;
    this.access = access;
    this.bulkAdd = bulkAdd;
  }

  @PostMapping("/bulk")
  public BulkAddMembersService.Result bulk(
      @PathVariable String listId, @RequestBody BulkBody body) {
    lists
        .findById(listId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "list not found"));
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    boolean senderOnly =
        auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + UserRole.LIST_SENDER.name()));
    boolean elevated =
        auth.getAuthorities().stream()
            .anyMatch(
                a ->
                    a.getAuthority().equals("ROLE_" + UserRole.ADMIN.name())
                        || a.getAuthority().equals("ROLE_" + UserRole.LIST_MANAGER.name()));
    if (senderOnly && !elevated) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "List senders cannot bulk-add");
    }
    if (!access.canManage(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "manage grant required");
    }
    return bulkAdd.add(listId, body == null ? null : body.text());
  }

  public record BulkBody(String text) {}
}
