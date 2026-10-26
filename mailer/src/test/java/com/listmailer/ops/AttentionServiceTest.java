package com.listmailer.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AttentionServiceTest {

  @Mock AttentionItemRepository items;
  @Mock ListAccessService access;
  @InjectMocks AttentionService service;

  @Test
  void listSenderForbidden() {
    when(access.isListSenderOnly()).thenReturn(true);
    assertThrows(ResponseStatusException.class, service::inboxForCurrentUser);
  }

  @Test
  void adminSeesAllIncludingOps() {
    when(access.isListSenderOnly()).thenReturn(false);
    when(access.isAdmin()).thenReturn(true);
    when(items.findByOpenTrueOrderByCreatedAtDesc())
        .thenReturn(
            List.of(
                new AttentionItemEntity("1", "t", "d", "OPS_ERROR", null, Instant.now()),
                new AttentionItemEntity("2", "t2", "d2", "GENERIC", "L1", Instant.now())));
    assertEquals(2, service.inboxForCurrentUser().size());
  }

  @Test
  void listManagerScopedNoGlobalOps() {
    when(access.isListSenderOnly()).thenReturn(false);
    when(access.isAdmin()).thenReturn(false);
    when(access.isListManager()).thenReturn(true);
    when(access.managedListIds()).thenReturn(List.of("L1"));
    when(items.findByOpenTrueAndListIdInOrderByCreatedAtDesc(List.of("L1")))
        .thenReturn(List.of(new AttentionItemEntity("2", "t2", "d2", "GENERIC", "L1", Instant.now())));
    assertEquals(1, service.inboxForCurrentUser().size());
    assertEquals("L1", service.inboxForCurrentUser().get(0).getListId());
  }
}
