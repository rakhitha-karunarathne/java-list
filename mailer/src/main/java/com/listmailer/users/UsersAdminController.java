package com.listmailer.users;

import com.listmailer.kernel.ids.Guids;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UsersAdminController {

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;

  public UsersAdminController(UserRepository users, PasswordEncoder passwordEncoder) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping
  public List<UserResponse> list() {
    return users.findAll().stream().map(UserResponse::from).toList();
  }

  @PostMapping
  public UserResponse create(@RequestBody CreateUserRequest body) {
    if (!StringUtils.hasText(body.email())
        || !StringUtils.hasText(body.password())
        || body.role() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email, password, and role required");
    }
    String email = body.email().trim().toLowerCase();
    if (users.findByEmailIgnoreCase(email).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");
    }
    try {
      UserEntity saved =
          users.save(
              new UserEntity(
                  Guids.newGuid(),
                  email,
                  passwordEncoder.encode(body.password()),
                  body.role(),
                  true,
                  Instant.now()));
      return UserResponse.from(saved);
    } catch (DataIntegrityViolationException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "email already exists");
    }
  }

  @PatchMapping("/{id}")
  public UserResponse patch(@PathVariable String id, @RequestBody PatchUserRequest body) {
    UserEntity user =
        users
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    boolean enabled = body.enabled() != null ? body.enabled() : user.isEnabled();
    UserRole role = body.role() != null ? body.role() : user.getRole();
    boolean removingLastAdmin =
        user.getRole() == UserRole.ADMIN
            && user.isEnabled()
            && !(role == UserRole.ADMIN && enabled)
            && countActiveAdminsExcluding(user.getId()) == 0;
    if (removingLastAdmin) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot remove the last Admin");
    }
    user.setEnabled(enabled);
    user.setRole(role);
    return UserResponse.from(users.save(user));
  }

  private long countActiveAdminsExcluding(String excludeId) {
    return users.findAll().stream()
        .filter(u -> u.getRole() == UserRole.ADMIN && u.isEnabled() && !u.getId().equals(excludeId))
        .count();
  }

  public record CreateUserRequest(String email, String password, UserRole role) {}

  public record PatchUserRequest(Boolean enabled, UserRole role) {}

  public record UserResponse(String id, String email, UserRole role, boolean enabled) {
    static UserResponse from(UserEntity u) {
      return new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.isEnabled());
    }
  }
}
