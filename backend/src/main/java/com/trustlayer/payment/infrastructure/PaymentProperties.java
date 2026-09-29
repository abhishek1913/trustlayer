package com.trustlayer.payment.infrastructure;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trustlayer.payment")
public record PaymentProperties(String webhookSecret, String successUrl, String cancelUrl, Map<String, String> priceIds) {
}
