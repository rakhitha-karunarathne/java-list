package com.listmailer.members;

import com.listmailer.kernel.ids.Guids;
import java.time.Instant;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BulkAddMembersService {

  private static final Pattern EMAIL =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

  private final MemberRepository members;
  private final ListMemberRepository listMembers;

  public BulkAddMembersService(MemberRepository members, ListMemberRepository listMembers) {
    this.members = members;
    this.listMembers = listMembers;
  }

  @Transactional
  public Result add(String listId, String paste) {
    int added = 0;
    int invalid = 0;
    int duplicateInPaste = 0;
    int alreadyOnList = 0;
    int unsubscribed = 0;
    Set<String> seen = new HashSet<>();
    String[] lines = paste == null ? new String[0] : paste.split("[\\r\\n,;]+");
    for (String raw : lines) {
      String email = raw.trim().toLowerCase(Locale.ROOT);
      if (email.isEmpty()) {
        continue;
      }
      if (!EMAIL.matcher(email).matches()) {
        invalid++;
        continue;
      }
      if (!seen.add(email)) {
        duplicateInPaste++;
        continue;
      }
      MemberEntity member =
          members
              .findByEmailIgnoreCase(email)
              .orElseGet(
                  () ->
                      members.save(
                          new MemberEntity(Guids.newGuid(), email, true, Instant.now())));
      var existing = listMembers.findByListIdAndMemberId(listId, member.getId());
      if (existing.isPresent()) {
        if (existing.get().isUnsubscribed()) {
          unsubscribed++;
        } else {
          alreadyOnList++;
        }
        continue;
      }
      listMembers.save(new ListMemberEntity(listId, member.getId(), false, Instant.now()));
      added++;
    }
    return new Result(added, invalid, duplicateInPaste, alreadyOnList, unsubscribed);
  }

  public record Result(
      int added, int invalid, int duplicateInPaste, int alreadyOnList, int unsubscribed) {}
}
