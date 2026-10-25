package com.listmailer.users;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.kernel.security.AccessTokenService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me/mfa")
public class MfaController {

  private final UserRepository users;
  private final MfaService mfa;
  private final ParametersPort parameters;
  private final AccessTokenService tokens;

  public MfaController(
      UserRepository users,
      MfaService mfa,
      ParametersPort parameters,
      AccessTokenService tokens) {
    this.users = users;
    this.mfa = mfa;
    this.parameters = parameters;
    this.tokens = tokens;
  }

  @PostMapping("/setup")
  public Map<String, String> setup(Authentication auth) {
    UserEntity user = current(auth);
    try {
      MfaService.Setup setup = mfa.startSetup(user);
      return Map.of("secret", setup.secret(), "otpAuthUrl", setup.otpAuthUrl());
    } catch (IllegalStateException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
    }
  }

  @PostMapping("/confirm")
  public Map<String, Object> confirm(Authentication auth, @RequestBody CodeRequest body) {
    UserEntity user = current(auth);
    try {
      List<String> codes = mfa.confirmSetup(user, body.code());
      Map<String, Object> out = new LinkedHashMap<>();
      out.put("enabled", true);
      out.put("backupCodes", codes);
      if (auth.getPrincipal() instanceof Jwt jwt && "enroll".equals(jwt.getClaimAsString("purpose"))) {
        out.put("token", tokens.accessToken(user));
      }
      return out;
    } catch (IllegalStateException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
  }

  @DeleteMapping
  public Map<String, Boolean> unenroll(Authentication auth, @RequestBody CodeRequest body) {
    if (parameters
        .get(ParameterKeys.MFA_MANDATORY)
        .map(v -> "true".equalsIgnoreCase(v))
        .orElse(false)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "MFA is mandatory");
    }
    UserEntity user = current(auth);
    try {
      mfa.unenroll(user, body.code());
      return Map.of("enabled", false);
    } catch (IllegalStateException ex) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }
  }

  private UserEntity current(Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    return users
        .findById(jwt.getSubject())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  public record CodeRequest(String code) {}
}
