package com.listmailer.kernel.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({BootstrapAuthProperties.class, JwtProperties.class})
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, EnabledUserFilter enabledUserFilter)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/api/auth/login", "/api/auth/mfa")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/info")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/favicon.svg")
                    .permitAll()
                    .requestMatchers("/t/**", "/dl/**", "/unsubscribe/**", "/approve/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/users/**",
                        "/api/parameters/**",
                        "/api/sending-paths/**",
                        "/api/bounce-mailboxes/**",
                        "/api/bounce-process/**",
                        "/api/footers/**",
                        "/api/notice-sending-path/**",
                        "/api/mts/**",
                        "/api/ops-logs/**",
                        "/api/activity-logs/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .oauth2ResourceServer(
            oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(new RoleJwtConverter())))
        .addFilterAfter(enabledUserFilter, BearerTokenAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  JwtEncoder jwtEncoder(JwtProperties props) {
    SecretKey key = secretKey(props);
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  JwtDecoder jwtDecoder(JwtProperties props) {
    return NimbusJwtDecoder.withSecretKey(secretKey(props)).macAlgorithm(MacAlgorithm.HS256).build();
  }

  private static SecretKey secretKey(JwtProperties props) {
    byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      throw new IllegalStateException("mailer.jwt.secret must be at least 32 bytes");
    }
    return new SecretKeySpec(bytes, "HmacSHA256");
  }
}
