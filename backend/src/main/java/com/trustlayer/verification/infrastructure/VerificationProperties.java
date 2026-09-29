package com.trustlayer.verification.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trustlayer.verification")
public record VerificationProperties(
        String provider,
        Duration sessionTtl,
        int maxAttempts,
        String mockWebhookSecret,
        String stripeWebhookSecret) {
}
