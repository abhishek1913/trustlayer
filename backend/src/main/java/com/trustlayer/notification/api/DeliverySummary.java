package com.trustlayer.notification.api;

import java.time.Instant;
import java.util.UUID;

public record DeliverySummary(
        UUID id,
        UUID userId,
        String eventType,
        String channel,
        String status,
        int attempts,
        String lastError,
        Instant sentAt,
        Instant createdAt) {
}
