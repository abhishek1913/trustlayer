package com.trustlayer.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trustlayer.stripe")
public record StripeProperties(String apiKey, String apiBase, int connectTimeoutMillis, int readTimeoutMillis) {

    public boolean configured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
