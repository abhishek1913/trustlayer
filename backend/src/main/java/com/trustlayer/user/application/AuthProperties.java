package com.trustlayer.user.application;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trustlayer.auth")
public record AuthProperties(
        Duration refreshTtl,
        Duration emailVerificationTtl,
        Duration passwordResetTtl,
        int maxFailedLogins,
        Duration lockDuration) {
}
