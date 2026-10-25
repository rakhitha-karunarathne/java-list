package com.listmailer.members;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListMemberRepository extends JpaRepository<ListMemberEntity, ListMemberEntity.Pk> {

  Optional<ListMemberEntity> findByListIdAndMemberId(String listId, String memberId);

  List<ListMemberEntity> findByListId(String listId);

  List<ListMemberEntity> findByMemberId(String memberId);

  List<ListMemberEntity> findByListIdIn(Iterable<String> listIds);
}
