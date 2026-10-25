package com.listmailer.members;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {

  Optional<MemberEntity> findByEmailIgnoreCase(String email);

  List<MemberEntity> findByEmailContainingIgnoreCase(String email);
}
