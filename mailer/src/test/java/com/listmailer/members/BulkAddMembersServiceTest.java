package com.listmailer.members;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkAddMembersServiceTest {

  @Mock MemberRepository members;
  @Mock ListMemberRepository listMembers;
  @InjectMocks BulkAddMembersService service;

  @Test
  void countsInvalidDupesAndAdds() {
    when(members.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
    when(members.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(listMembers.findByListIdAndMemberId(anyString(), anyString()))
        .thenReturn(Optional.empty());
    when(listMembers.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result =
        service.add(
            "list-1",
            "good@example.com\nbad\ngood@example.com\nalso@example.com");
    assertEquals(2, result.added());
    assertEquals(1, result.invalid());
    assertEquals(1, result.duplicateInPaste());
  }
}
