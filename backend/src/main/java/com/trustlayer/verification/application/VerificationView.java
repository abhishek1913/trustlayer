package com.trustlayer.verification.application;

import com.trustlayer.verification.domain.VerificationSession;
import java.time.Instant;
import java.util.UUID;

public record VerificationView(
        UUID id,
        String status,
        String provider,
        boolean mock,
        Instant expiresAt,
        String redirectUrl,
        String notice) {

    static final String MOCK_NOTICE =
            "MOCK provider: no real document or selfie check is performed. Decide the outcome with the admin mock-decision endpoint.";

    static VerificationView of(VerificationSession session, boolean mock, String redirectUrl) {
        return new VerificationView(session.getId(), session.getStatus().name(), session.getProvider(), mock,
                session.getExpiresAt(), redirectUrl, mock ? MOCK_NOTICE : null);
    }
}
