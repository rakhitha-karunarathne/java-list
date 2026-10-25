package com.listmailer.kernel.security;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.users.MfaService;
import com.listmailer.users.UserEntity;
import com.listmailer.users.UserRepository;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository users;
  private final PasswordEncoder passwordEncoder;
  private final JwtDecoder jwtDecoder;
  private final MfaService mfa;
  private final MfaTokenStore mfaTokens;
  private final ParametersPort parameters;
  private final AccessTokenService tokens;

  public AuthController(
      UserRepository users,
      PasswordEncoder passwordEncoder,
      JwtDecoder jwtDecoder,
      MfaService mfa,
      MfaTokenStore mfaTokens,
      ParametersPort parameters,
      AccessTokenService tokens) {
    this.users = users;
    this.passwordEncoder = passwordEncoder;
    this.jwtDecoder = jwtDecoder;
    this.mfa = mfa;
    this.mfaTokens = mfaTokens;
    this.parameters = parameters;
    this.tokens = tokens;
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody LoginRequest body) {
    UserEntity user = authenticatePassword(body.email(), body.password());
    if (user.isTotpEnabled()) {
      return Map.of("mfaRequired", true, "mfaToken", tokens.mfaToken(user));
    }
    if (mfaMandatory()) {
      return Map.of("enrollmentRequired", true, "enrollmentToken", tokens.enrollmentToken(user));
    }
    return Map.of("token", tokens.accessToken(user));
  }

  @PostMapping("/mfa")
  public Map<String, String> mfa(@RequestBody MfaRequest body) {
    if (body.mfaToken() == null || body.code() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA");
    }
    Jwt jwt;
    try {
      jwt = jwtDecoder.decode(body.mfaToken());
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA");
    }
    if (!"mfa".equals(jwt.getClaimAsString("purpose"))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA");
    }
    String jti = jwt.getId();
    if (jti == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA");
    }
    UserEntity user =
        users
            .findById(jwt.getSubject())
            .filter(UserEntity::isEnabled)
            .filter(UserEntity::isTotpEnabled)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA"));
    if (!mfa.verifyCode(user, body.code()) || !mfaTokens.consume(jti)) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid MFA");
    }
    return Map.of("token", tokens.accessToken(user));
  }

  private boolean mfaMandatory() {
    return parameters
        .get(ParameterKeys.MFA_MANDATORY)
        .map(v -> "true".equalsIgnoreCase(v))
        .orElse(false);
  }

  private UserEntity authenticatePassword(String email, String password) {
    if (email == null || password == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    UserEntity user =
        users
            .findByEmailIgnoreCase(email.trim())
            .filter(UserEntity::isEnabled)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    return user;
  }

  public record LoginRequest(String email, String password) {}

  public record MfaRequest(String mfaToken, String code) {}
}
