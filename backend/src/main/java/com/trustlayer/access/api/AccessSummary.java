package com.trustlayer.access.api;

import java.time.Instant;

public record AccessSummary(
        String state,
        boolean emailVerified,
        boolean identityVerified,
        boolean paymentSucceeded,
        boolean subscriptionActive,
        Instant updatedAt) {
}
