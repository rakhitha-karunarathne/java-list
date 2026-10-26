package com.listmailer.consent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class ConsentServiceTest {

  @Mock OpaqueTokenHelper tokens;
  @Mock MemberRepository members;
  @Mock ListMemberRepository listMembers;
  @Mock ListAccessService access;
  @Mock NoticeMailService noticeMail;
  @Mock ParametersPort parameters;
  @Mock OpsHooksPort ops;
  @InjectMocks ConsentService service;

  @Test
  void requestSendsNoticeMailEvenIfDisabled() {
    MemberEntity m = new MemberEntity("m1", "a@x.com", false, Instant.now());
    m.setDisableReason("bounce");
    when(members.findById("m1")).thenReturn(Optional.of(m));
    when(listMembers.findByMemberId("m1"))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", true, Instant.now())));
    when(access.isAdmin()).thenReturn(true);
    when(parameters.get(ParameterKeys.PUBLIC_BASE_URL))
        .thenReturn(Optional.of("https://lists.example.com"));
    when(parameters.get("consent.approve.expiry.days")).thenReturn(Optional.empty());
    when(tokens.seal(any())).thenReturn("sealed");

    service.requestClearUnsub("m1", null);

    verify(noticeMail)
        .send(eq("a@x.com"), anyString(), contains("/approve/sealed"), contains("/approve/sealed"));
    verify(listMembers, never()).save(any());
  }

  @Test
  void approveClearsUnsub() {
    long exp = Instant.now().toEpochMilli() + TimeUnit.DAYS.toMillis(1);
    byte[] payload =
        ConsentTokenCodec.encode(new ConsentTokenCodec.Payload("m1", List.of("L1"), exp));
    when(tokens.open("tok")).thenReturn(payload);
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@x.com", true, Instant.now())));
    ListMemberEntity lm = new ListMemberEntity("L1", "m1", true, Instant.now());
    when(listMembers.findByListIdAndMemberId("L1", "m1")).thenReturn(Optional.of(lm));

    assertEquals(ConsentService.ApproveOutcome.CLEARED, service.approve("tok"));
    assertFalse(lm.isUnsubscribed());
    verify(ops).audit(eq("consent.approve"), contains("member=m1"));
  }

  @Test
  void expiredDoesNotClear() {
    long exp = Instant.now().toEpochMilli() - 1000;
    byte[] payload =
        ConsentTokenCodec.encode(new ConsentTokenCodec.Payload("m1", List.of("L1"), exp));
    when(tokens.open("tok")).thenReturn(payload);

    assertEquals(ConsentService.ApproveOutcome.EXPIRED, service.approve("tok"));
    verify(listMembers, never()).save(any());
  }

  @Test
  void reuseApproveAfterClearIsIdempotentNoAudit() {
    long exp = Instant.now().toEpochMilli() + TimeUnit.DAYS.toMillis(1);
    byte[] payload =
        ConsentTokenCodec.encode(new ConsentTokenCodec.Payload("m1", List.of("L1"), exp));
    when(tokens.open("tok")).thenReturn(payload);
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@x.com", true, Instant.now())));
    ListMemberEntity lm = new ListMemberEntity("L1", "m1", false, Instant.now());
    when(listMembers.findByListIdAndMemberId("L1", "m1")).thenReturn(Optional.of(lm));

    assertEquals(ConsentService.ApproveOutcome.ALREADY_CLEAR, service.approve("tok"));
    verify(listMembers, never()).save(any());
    verify(ops, never()).audit(anyString(), anyString());
  }

  @Test
  void requestRejectedWithoutManage() {
    when(members.findById("m1"))
        .thenReturn(Optional.of(new MemberEntity("m1", "a@x.com", true, Instant.now())));
    when(listMembers.findByMemberId("m1"))
        .thenReturn(List.of(new ListMemberEntity("L1", "m1", true, Instant.now())));
    when(access.isAdmin()).thenReturn(false);
    when(access.canManage("L1")).thenReturn(false);

    assertThrows(ResponseStatusException.class, () -> service.requestClearUnsub("m1", null));
    verify(noticeMail, never()).send(anyString(), anyString(), anyString(), anyString());
  }
}
