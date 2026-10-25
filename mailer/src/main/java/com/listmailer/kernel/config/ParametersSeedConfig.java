package com.listmailer.kernel.config;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParametersSeedConfig {

  @Bean
  ApplicationRunner seedParameters(
      ParametersPort parameters,
      @Value("${mailer.public-base-url}") String publicBaseUrl) {
    return args -> {
      if (!publicBaseUrl.startsWith("https://")) {
        throw new IllegalStateException(
            "mailer.public-base-url must start with https:// (got: " + publicBaseUrl + ")");
      }
      seedIfAbsent(parameters, ParameterKeys.PUBLIC_BASE_URL, publicBaseUrl);
      seedIfAbsent(parameters, ParameterKeys.MFA_MANDATORY, "false");
      seedIfAbsent(parameters, ParameterKeys.VERP_ENABLED, "false");
      seedIfAbsent(parameters, ParameterKeys.OPS_LOG_RETENTION_DAYS, "90");
      seedIfAbsent(parameters, ParameterKeys.AUDIT_RETENTION_DAYS, "365");
    };
  }

  static void seedIfAbsent(ParametersPort parameters, String key, String value) {
    if (parameters.get(key).isEmpty()) {
      parameters.set(key, value);
    }
  }
}
