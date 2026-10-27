package com.listmailer.users;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me")
public class MeAccountController {

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;

  public MeAccountController(UserRepository users, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping
  public Map<String, Object> me(Authentication auth) {
    UserEntity user = current(auth);
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("email", user.getEmail());
    out.put("role", user.getRole().name());
    out.put("timeZone", normalizeStoredZone(user.getTimeZone()));
    return out;
  }

  @PutMapping("/time-zone")
  public Map<String, String> setTimeZone(
      Authentication auth, @RequestBody(required = false) TimeZoneRequest body) {
    UserEntity user = current(auth);
    if (body == null || !StringUtils.hasText(body.timeZone())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "timeZone required");
    }
    String id = requireIanaZone(body.timeZone().trim());
    user.setTimeZone(id);
    users.save(user);
    return Map.of("timeZone", id);
  }

  /** IANA region ids only (ZoneId.getAvailableZoneIds), not offsets or short aliases. */
  static String requireIanaZone(String id) {
    try {
      ZoneId.of(id);
    } catch (DateTimeException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid timeZone");
    }
    if (!ZoneId.getAvailableZoneIds().contains(id)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid timeZone");
    }
    return id;
  }

  static String normalizeStoredZone(String stored) {
    if (!StringUtils.hasText(stored)) {
      return "UTC";
    }
    String id = stored.trim();
    return ZoneId.getAvailableZoneIds().contains(id) ? id : "UTC";
  }

  @PostMapping("/password")
  public Map<String, Boolean> changePassword(
      Authentication auth, @RequestBody(required = false) PasswordChangeRequest body) {
    UserEntity user = current(auth);
    if (body == null
        || !StringUtils.hasText(body.currentPassword())
        || !StringUtils.hasText(body.newPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "current and new password required");
    }
    if (!passwordEncoder.matches(body.currentPassword(), user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "current password is wrong");
    }
    if (body.currentPassword().equals(body.newPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "new password must differ");
    }
    user.setPasswordHash(passwordEncoder.encode(body.newPassword()));
    users.save(user);
    return Map.of("changed", true);
  }

  private UserEntity current(Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return users
        .findById(jwt.getSubject())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  public record PasswordChangeRequest(String currentPassword, String newPassword) {}

  public record TimeZoneRequest(String timeZone) {}
}
