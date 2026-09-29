package com.trustlayer.shared.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trustlayer.jwt")
public record JwtProperties(String secret, Duration accessTtl, String issuer) {
}
