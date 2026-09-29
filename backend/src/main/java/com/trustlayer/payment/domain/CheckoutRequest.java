package com.trustlayer.payment.domain;

import java.util.UUID;

public record CheckoutRequest(UUID transactionId, UUID userId, String userEmail, Plan plan, String idempotencyKey) {
}
