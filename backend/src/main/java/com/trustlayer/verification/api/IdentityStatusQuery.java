package com.trustlayer.verification.api;

import java.util.Optional;
import java.util.UUID;

public interface IdentityStatusQuery {

    boolean isVerified(UUID userId);

    Optional<VerificationSummary> latestFor(UUID userId);
}
