package com.listmailer.unsubscribe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.token.OpaqueTokenHelper;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberEntity;
import com.listmailer.members.MemberRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UnsubscribeServiceTest {

  @Mock OpaqueTokenHelper tokens;
  @Mock ListMemberRepository listMembers;
  @Mock MemberRepository members;
  @Mock OpsHooksPort ops;
  @InjectMocks UnsubscribeService service;

  @Test
  void validTokenUnsubscribesAllSnapshotListsWithoutFlippingEnabled() {
    byte[] payload =
        UnsubTokenCodec.encode(
            new UnsubTokenCodec.Payload("m1", "c1", List.of("l1", "l2"), "q1"));
    when(tokens.open("tok")).thenReturn(payload);
    MemberEntity member = new MemberEntity("m1", "a@example.com", true, Instant.now());
    when(members.findById("m1")).thenReturn(Optional.of(member));
    ListMemberEntity lm1 = new ListMemberEntity("l1", "m1", false, Instant.now());
    ListMemberEntity lm2 = new ListMemberEntity("l2", "m1", false, Instant.now());
    when(listMembers.findByListIdAndMemberId("l1", "m1")).thenReturn(Optional.of(lm1));
    when(listMembers.findByListIdAndMemberId("l2", "m1")).thenReturn(Optional.of(lm2));

    assertEquals(UnsubscribeService.Outcome.UNSUBSCRIBED, service.apply("tok"));
    assertTrue(lm1.isUnsubscribed());
    assertTrue(lm2.isUnsubscribed());
    assertTrue(member.isEnabled());
    verify(ops).audit(eq("unsubscribe"), contains("member=m1"));
    verify(ops, never()).audit(eq("unsubscribe"), contains("tok"));
  }

  @Test
  void doesNotUnsubListsOutsideSnapshot() {
    byte[] payload =
        UnsubTokenCodec.encode(new UnsubTokenCodec.Payload("m1", "c1", List.of("l1"), "q1"));
    when(tokens.open("tok")).thenReturn(payload);
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@example.com", true, Instant.now())));
    ListMemberEntity lm1 = new ListMemberEntity("l1", "m1", false, Instant.now());
    when(listMembers.findByListIdAndMemberId("l1", "m1")).thenReturn(Optional.of(lm1));

    service.apply("tok");
    verify(listMembers, never()).findByListIdAndMemberId(eq("l-out"), anyString());
    assertTrue(lm1.isUnsubscribed());
  }

  @Test
  void invalidTokenIsSafeNoMembershipChange() {
    when(tokens.open(any())).thenThrow(new IllegalArgumentException("bad"));
    assertEquals(UnsubscribeService.Outcome.INVALID, service.apply("garbage"));
    verify(listMembers, never()).save(any());
    verify(ops, never()).audit(anyString(), anyString());
  }

  @Test
  void disabledMemberStaysDisabled() {
    byte[] payload =
        UnsubTokenCodec.encode(new UnsubTokenCodec.Payload("m1", "c1", List.of("l1"), "q1"));
    when(tokens.open("tok")).thenReturn(payload);
    MemberEntity member = new MemberEntity("m1", "a@example.com", false, Instant.now());
    member.setDisableReason("bounce");
    when(members.findById("m1")).thenReturn(Optional.of(member));
    ListMemberEntity lm1 = new ListMemberEntity("l1", "m1", false, Instant.now());
    when(listMembers.findByListIdAndMemberId("l1", "m1")).thenReturn(Optional.of(lm1));

    service.apply("tok");
    assertFalse(member.isEnabled());
    assertEquals("bounce", member.getDisableReason());
  }
}
