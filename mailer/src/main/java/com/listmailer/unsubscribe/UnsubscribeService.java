package com.listmailer.unsubscribe;

import com.listmailer.kernel.ports.OpsHooksPort;
import com.listmailer.kernel.token.OpaqueTokenHelper;
import com.listmailer.members.ListMemberEntity;
import com.listmailer.members.ListMemberRepository;
import com.listmailer.members.MemberRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class UnsubscribeService {

  public enum Outcome {
    UNSUBSCRIBED,
    ALREADY,
    INVALID
  }

  private final OpaqueTokenHelper tokens;
  private final ListMemberRepository listMembers;
  private final MemberRepository members;
  private final OpsHooksPort ops;

  public UnsubscribeService(
      OpaqueTokenHelper tokens,
      ListMemberRepository listMembers,
      MemberRepository members,
      OpsHooksPort ops) {
    this.tokens = tokens;
    this.listMembers = listMembers;
    this.members = members;
    this.ops = ops;
  }

  @Transactional
  public Outcome apply(String rawToken) {
    if (!StringUtils.hasText(rawToken)) {
      return Outcome.INVALID;
    }
    UnsubTokenCodec.Payload payload;
    try {
      payload = UnsubTokenCodec.decode(tokens.open(rawToken.trim()));
    } catch (RuntimeException ex) {
      return Outcome.INVALID;
    }
    if (members.findById(payload.memberId()).isEmpty()) {
      return Outcome.INVALID;
    }
    int changed = 0;
    int already = 0;
    for (String listId : payload.listIds()) {
      Optional<ListMemberEntity> row =
          listMembers.findByListIdAndMemberId(listId, payload.memberId());
      if (row.isEmpty()) {
        continue;
      }
      ListMemberEntity lm = row.get();
      if (lm.isUnsubscribed()) {
        already++;
        continue;
      }
      // C3: only flip per-list unsubscribed — never Member.enabled
      lm.setUnsubscribed(true);
      listMembers.save(lm);
      changed++;
    }
    if (changed == 0 && already == 0 && !payload.listIds().isEmpty()) {
      // member not on any snapshot lists — treat as invalid/non-enumerating
      return Outcome.INVALID;
    }
    if (changed > 0) {
      ops.audit(
          "unsubscribe",
          "member="
              + payload.memberId()
              + " campaign="
              + payload.campaignId()
              + " lists="
              + changed);
      return Outcome.UNSUBSCRIBED;
    }
    return Outcome.ALREADY;
  }
}
