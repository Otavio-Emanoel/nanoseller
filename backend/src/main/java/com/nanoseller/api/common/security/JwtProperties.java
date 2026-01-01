package com.nanoseller.api.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenTtl,
        String issuer
) {
    public JwtProperties {
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofHours(2);
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "nanoseller-api";
        }
    }
}
