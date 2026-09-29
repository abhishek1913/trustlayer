package com.trustlayer.shared.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
class StripeConfiguration {

    private final StripeProperties properties;

    StripeConfiguration(StripeProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        if (properties.configured()) {
            Stripe.apiKey = properties.apiKey();
        }
        if (properties.apiBase() != null && !properties.apiBase().isBlank()) {
            Stripe.overrideApiBase(properties.apiBase());
        }
        Stripe.setConnectTimeout(properties.connectTimeoutMillis());
        Stripe.setReadTimeout(properties.readTimeoutMillis());
    }
}
