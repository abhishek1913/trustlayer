package com.trustlayer.payment.api;

import java.time.Instant;
import java.util.UUID;

public record PaymentSummary(
        UUID id,
        UUID userId,
        String planCode,
        String status,
        long amountCents,
        String currency,
        Instant createdAt) {
}
