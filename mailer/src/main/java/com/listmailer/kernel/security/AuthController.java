package com.listmailer.kernel.security;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final BootstrapAuthProperties bootstrap;
  private final JwtEncoder jwtEncoder;

  public AuthController(BootstrapAuthProperties bootstrap, JwtEncoder jwtEncoder) {
    this.bootstrap = bootstrap;
    this.jwtEncoder = jwtEncoder;
  }

  @PostMapping("/login")
  public Map<String, String> login(@RequestBody LoginRequest body) {
    if (body.email() == null
        || body.password() == null
        || !body.email().equalsIgnoreCase(bootstrap.email())
        || !body.password().equals(bootstrap.password())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    Instant now = Instant.now();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer("list-mailer")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(8 * 3600))
            .subject(bootstrap.email())
            .claim("role", "ADMIN")
            .build();
    String token =
        jwtEncoder
            .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
            .getTokenValue();
    return Map.of("token", token);
  }

  public record LoginRequest(String email, String password) {}
}
