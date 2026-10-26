package com.listmailer.ops;

import com.listmailer.users.ListAccessService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AttentionService {

  private final AttentionItemRepository items;
  private final ListAccessService access;

  public AttentionService(AttentionItemRepository items, ListAccessService access) {
    this.items = items;
    this.access = access;
  }

  public List<AttentionItemEntity> inboxForCurrentUser() {
    if (access.isListSenderOnly()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Attention not available");
    }
    if (access.isAdmin()) {
      return items.findByOpenTrueOrderByCreatedAtDesc();
    }
    if (access.isListManager()) {
      List<String> lists = access.managedListIds();
      if (lists.isEmpty()) {
        return List.of();
      }
      // LM: list-scoped only — never global OPS_ERROR (list_id null)
      return items.findByOpenTrueAndListIdInOrderByCreatedAtDesc(lists);
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Attention not available");
  }

  @Transactional
  public void acknowledge(String id) {
    if (access.isListSenderOnly()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    AttentionItemEntity item =
        items.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!access.isAdmin()) {
      if (item.getListId() == null || !access.managedListIds().contains(item.getListId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
      }
    }
    item.setOpen(false);
    items.save(item);
  }
}
