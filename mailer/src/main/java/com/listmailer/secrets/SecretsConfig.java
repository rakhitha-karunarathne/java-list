package com.listmailer.secrets;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecretsProperties.class)
public class SecretsConfig {

  @Bean
  SecretsCrypto secretsCrypto(SecretsProperties props) {
    return new SecretsCrypto(props.getKeys());
  }
}
