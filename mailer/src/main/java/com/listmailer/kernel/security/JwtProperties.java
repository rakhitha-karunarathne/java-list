package com.listmailer.kernel.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mailer.jwt")
public record JwtProperties(String secret) {}
