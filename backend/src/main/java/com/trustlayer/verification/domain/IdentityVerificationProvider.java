package com.trustlayer.verification.domain;

import java.util.Optional;
import java.util.UUID;

public interface IdentityVerificationProvider {

    String name();

    boolean isMock();

    String signatureHeaderName();

    ProviderSession createSession(UUID userId);

    Optional<ProviderOutcome> getResult(String providerRef);

    ProviderWebhookEvent parseWebhook(String payload, String signature);
}
