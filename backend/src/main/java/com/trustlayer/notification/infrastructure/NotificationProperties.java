package com.trustlayer.notification.infrastructure;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trustlayer.notification")
public record NotificationProperties(
        String emailProvider,
        String whatsappProvider,
        boolean mockLogBody,
        boolean mockFail,
        int maxAttempts,
        Duration backoff,
        String mailFrom,
        Meta meta) {

    public record Meta(String apiBase, String apiVersion, String phoneNumberId, String accessToken) {
    }
}
