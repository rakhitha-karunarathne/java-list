package com.listmailer.lists;

import com.listmailer.users.ListAccessService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Temporary list access probe until lists epic owns full CRUD. */
@RestController
@RequestMapping("/api/lists")
public class ListsAccessController {

  private final ListAccessService access;

  public ListsAccessController(ListAccessService access) {
    this.access = access;
  }

  @GetMapping("/{listId}")
  public Map<String, Object> get(@PathVariable String listId) {
    if (!access.canAccess(listId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no grant for list");
    }
    return Map.of(
        "id",
        listId,
        "canManage",
        access.canManage(listId),
        "canSend",
        access.canSend(listId));
  }
}
