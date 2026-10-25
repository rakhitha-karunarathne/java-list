package com.listmailer.kernel.security;

import com.listmailer.users.UserEntity;
import com.listmailer.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rejects missing/disabled users and refreshes ROLE_* from DB so role changes apply immediately. */
@Component
public class EnabledUserFilter extends OncePerRequestFilter {

  private final UserRepository users;

  public EnabledUserFilter(UserRepository users) {
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
      UserEntity user =
          users.findById(jwt.getSubject()).filter(UserEntity::isEnabled).orElse(null);
      if (user == null) {
        response.sendError(HttpStatus.UNAUTHORIZED.value());
        return;
      }
      var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
      SecurityContextHolder.getContext()
          .setAuthentication(new JwtAuthenticationToken(jwt, authorities));
    }
    filterChain.doFilter(request, response);
  }
}
