package com.listmailer.kernel.config;

import com.listmailer.kernel.ports.ParameterKeys;
import com.listmailer.kernel.ports.ParametersPort;
import com.listmailer.parameters.ParameterDefinitions;
import com.listmailer.parameters.ParameterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParametersSeedConfig {

  @Bean
  ApplicationRunner seedParameters(
      ParametersPort parameters,
      ParameterRepository repository,
      @Value("${mailer.public-base-url}") String publicBaseUrl) {
    return args -> {
      if (!publicBaseUrl.startsWith("https://")) {
        throw new IllegalStateException(
            "mailer.public-base-url must start with https:// (got: " + publicBaseUrl + ")");
      }
      for (ParameterDefinitions.Def def : ParameterDefinitions.all().values()) {
        String value =
            ParameterKeys.PUBLIC_BASE_URL.equals(def.key()) ? publicBaseUrl : def.defaultValue();
        seedIfAbsent(parameters, def.key(), value);
        repository
            .findById(def.key())
            .ifPresent(
                row -> {
                  if (!def.description().equals(row.getDescription())) {
                    row.setDescription(def.description());
                    repository.save(row);
                  }
                });
      }
    };
  }

  static void seedIfAbsent(ParametersPort parameters, String key, String value) {
    if (parameters.get(key).isEmpty()) {
      parameters.set(key, value);
    }
  }
}
