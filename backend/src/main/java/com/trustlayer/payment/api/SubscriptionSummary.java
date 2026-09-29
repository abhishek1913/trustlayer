package com.trustlayer.payment.api;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionSummary(UUID id, UUID userId, String planCode, String status, Instant startedAt, Instant cancelledAt) {
}
