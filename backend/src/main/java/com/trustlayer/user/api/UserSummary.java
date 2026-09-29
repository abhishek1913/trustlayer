package com.trustlayer.user.api;

import java.time.Instant;
import java.util.UUID;

public record UserSummary(
        UUID id,
        String email,
        String fullName,
        String phoneNumber,
        boolean emailVerified,
        String role,
        Instant createdAt) {
}
