package com.listmailer.kernel.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mailer.bootstrap")
public record BootstrapAuthProperties(String email, String password) {}
