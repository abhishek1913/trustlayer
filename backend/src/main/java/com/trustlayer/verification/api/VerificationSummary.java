package com.trustlayer.verification.api;

import java.time.Instant;
import java.util.UUID;

public record VerificationSummary(UUID sessionId, String status, String provider, long attempts, Instant updatedAt) {
}
