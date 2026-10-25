package com.listmailer.users;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ListAccessServiceTest {

  @Mock UserRepository users;
  @Mock UserListGrantRepository grants;
  @InjectMocks ListAccessService access;

  @AfterEach
  void clearSecurity() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void adminBypassesGrants() {
    jwtAs("admin-id");
    when(users.findById("admin-id"))
        .thenReturn(
            Optional.of(
                new UserEntity(
                    "admin-id", "a@x.com", "h", UserRole.ADMIN, true, Instant.now())));
    assertTrue(access.canManage("any-list"));
    assertTrue(access.canSend("any-list"));
  }

  @Test
  void grantRequiredForNonAdmin() {
    jwtAs("u1");
    when(users.findById("u1"))
        .thenReturn(
            Optional.of(
                new UserEntity("u1", "s@x.com", "h", UserRole.LIST_SENDER, true, Instant.now())));
    when(grants.findByUserIdAndListId("u1", "list-1"))
        .thenReturn(Optional.of(new UserListGrantEntity("u1", "list-1", false, true)));
    assertFalse(access.canManage("list-1"));
    assertTrue(access.canSend("list-1"));
    assertFalse(access.canAccess("list-2"));
  }

  private static void jwtAs(String userId) {
    Jwt jwt =
        Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject(userId)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }
}
