package com.listmailer.kernel.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class RoleJwtConverterTest {

  @Test
  void mapsRoleClaimToAuthority() {
    Jwt jwt =
        Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject("user-1")
            .claim("role", "LIST_SENDER")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    List<String> authorities =
        new RoleJwtConverter()
            .convert(jwt).getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    assertEquals(List.of("ROLE_LIST_SENDER"), authorities);
  }

  @Test
  void blankRoleYieldsNoAuthorities() {
    Jwt jwt =
        Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject("user-1")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    assertTrue(new RoleJwtConverter().convert(jwt).getAuthorities().isEmpty());
  }
}
