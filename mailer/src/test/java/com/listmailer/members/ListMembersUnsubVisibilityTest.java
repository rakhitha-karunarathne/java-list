package com.listmailer.members;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.listmailer.lists.ListEntity;
import com.listmailer.lists.ListRepository;
import com.listmailer.users.ListAccessService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ListMembersUnsubVisibilityTest {

  @Mock ListRepository lists;
  @Mock ListAccessService access;
  @Mock BulkAddMembersService bulkAdd;
  @Mock ListMemberRepository listMembers;
  @Mock MemberRepository members;
  @InjectMocks ListMembersController controller;

  @Test
  void returnsUnsubscribedFlagWhenAllowed() {
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "A", "p", null, null, null, null, Instant.now())));
    when(access.canAccess("L1")).thenReturn(true);
    when(listMembers.findByListId("L1"))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", true, Instant.now())));
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@x.com", true, Instant.now())));

    var rows = controller.index("L1");
    assertEquals(1, rows.size());
    assertEquals(true, rows.get(0).unsubscribed());
  }

  @Test
  void forbiddenOutsideScope() {
    when(lists.findById("L1"))
        .thenReturn(
            Optional.of(new ListEntity("L1", "A", "p", null, null, null, null, Instant.now())));
    when(access.canAccess("L1")).thenReturn(false);
    when(access.isAdmin()).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> controller.index("L1"));
  }
}
