package com.listmailer.kernel.security;

import com.listmailer.users.UserEntity;
import com.listmailer.users.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Rejects JWTs for missing/disabled users so disable takes effect before expiry. */
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
      boolean ok =
          users.findById(jwt.getSubject()).filter(UserEntity::isEnabled).isPresent();
      if (!ok) {
        response.sendError(HttpStatus.UNAUTHORIZED.value());
        return;
      }
    }
    filterChain.doFilter(request, response);
  }
}
