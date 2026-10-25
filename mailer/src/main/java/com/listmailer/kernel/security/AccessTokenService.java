package com.listmailer.kernel.security;

import com.listmailer.kernel.ids.Guids;
import com.listmailer.users.UserEntity;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AccessTokenService {

  private final JwtEncoder jwtEncoder;

  public AccessTokenService(JwtEncoder jwtEncoder) {
    this.jwtEncoder = jwtEncoder;
  }

  public String accessToken(UserEntity user) {
    Instant now = Instant.now();
    return encode(
        JwtClaimsSet.builder()
            .issuer("list-mailer")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(8 * 3600))
            .subject(user.getId())
            .claim("email", user.getEmail())
            .claim("role", user.getRole().name())
            .build());
  }

  public String mfaToken(UserEntity user) {
    Instant now = Instant.now();
    return encode(
        JwtClaimsSet.builder()
            .id(Guids.newGuid())
            .issuer("list-mailer")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(300))
            .subject(user.getId())
            .claim("purpose", "mfa")
            .build());
  }

  public String enrollmentToken(UserEntity user) {
    Instant now = Instant.now();
    return encode(
        JwtClaimsSet.builder()
            .issuer("list-mailer")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(900))
            .subject(user.getId())
            .claim("purpose", "enroll")
            .build());
  }

  private String encode(JwtClaimsSet claims) {
    return jwtEncoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }
}
