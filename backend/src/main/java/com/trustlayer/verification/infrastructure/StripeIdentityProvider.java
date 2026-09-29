package com.trustlayer.verification.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.identity.VerificationSession;
import com.stripe.net.Webhook;
import com.stripe.param.identity.VerificationSessionCreateParams;
import com.trustlayer.shared.config.StripeProperties;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.verification.domain.IdentityVerificationProvider;
import com.trustlayer.verification.domain.ProviderOutcome;
import com.trustlayer.verification.domain.ProviderSession;
import com.trustlayer.verification.domain.ProviderWebhookEvent;
import com.trustlayer.verification.domain.VerificationStatus;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "trustlayer.verification.provider", havingValue = "stripe")
class StripeIdentityProvider implements IdentityVerificationProvider {

    private static final Logger log = LoggerFactory.getLogger(StripeIdentityProvider.class);

    private final StripeProperties stripe;
    private final VerificationProperties properties;
    private final ObjectMapper objectMapper;

    StripeIdentityProvider(StripeProperties stripe, VerificationProperties properties, ObjectMapper objectMapper) {
        this.stripe = stripe;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "stripe_identity";
    }

    @Override
    public boolean isMock() {
        return false;
    }

    @Override
    public String signatureHeaderName() {
        return "Stripe-Signature";
    }

    @Override
    public ProviderSession createSession(UUID userId) {
        requireConfigured();
        VerificationSessionCreateParams params = VerificationSessionCreateParams.builder()
                .setType(VerificationSessionCreateParams.Type.DOCUMENT)
                .setOptions(VerificationSessionCreateParams.Options.builder()
                        .setDocument(VerificationSessionCreateParams.Options.Document.builder()
                                .setRequireMatchingSelfie(true)
                                .build())
                        .build())
                .putMetadata("user_id", userId.toString())
                .build();
        try {
            VerificationSession session = VerificationSession.create(params);
            return new ProviderSession(session.getId(), session.getUrl());
        } catch (StripeException e) {
            log.warn("Stripe Identity session creation failed: {}", e.getClass().getSimpleName());
            throw new ApiException(ErrorCode.PROVIDER_ERROR, "Identity provider is unavailable");
        }
    }

    @Override
    public Optional<ProviderOutcome> getResult(String providerRef) {
        requireConfigured();
        try {
            VerificationSession session = VerificationSession.retrieve(providerRef);
            String lastError = session.getLastError() == null ? null : session.getLastError().getCode();
            return Optional.of(map(session.getStatus(), lastError));
        } catch (StripeException e) {
            log.warn("Stripe Identity retrieval failed: {}", e.getClass().getSimpleName());
            throw new ApiException(ErrorCode.PROVIDER_ERROR, "Identity provider is unavailable");
        }
    }

    @Override
    public ProviderWebhookEvent parseWebhook(String payload, String signature) {
        String secret = properties.stripeWebhookSecret();
        if (secret == null || secret.isBlank()) {
            throw new ApiException(ErrorCode.PROVIDER_NOT_CONFIGURED, "Identity webhook secret is not configured");
        }
        try {
            Webhook.constructEvent(payload, signature, secret);
        } catch (SignatureVerificationException | RuntimeException e) {
            throw new ApiException(ErrorCode.INVALID_WEBHOOK_SIGNATURE, "Invalid webhook signature");
        }
        try {
            JsonNode event = objectMapper.readTree(payload);
            String type = event.path("type").asText();
            JsonNode object = event.path("data").path("object");
            if (!type.startsWith("identity.verification_session.")) {
                return new ProviderWebhookEvent(event.path("id").asText(), type, null, null);
            }
            String status = type.substring("identity.verification_session.".length());
            String lastError = object.path("last_error").path("code").asText(null);
            return new ProviderWebhookEvent(
                    event.path("id").asText(), type, object.path("id").asText(null), map(status, lastError));
        } catch (Exception e) {
            throw new ApiException(ErrorCode.MALFORMED_REQUEST, "Malformed webhook payload");
        }
    }

    private ProviderOutcome map(String stripeStatus, String lastError) {
        return switch (stripeStatus) {
            case "verified" -> new ProviderOutcome(VerificationStatus.VERIFIED, null);
            case "requires_input" -> lastError == null
                    ? new ProviderOutcome(VerificationStatus.PENDING, null)
                    : new ProviderOutcome(VerificationStatus.REJECTED, lastError);
            case "processing" -> new ProviderOutcome(VerificationStatus.IN_PROGRESS, null);
            case "canceled" -> new ProviderOutcome(VerificationStatus.EXPIRED, "canceled");
            default -> new ProviderOutcome(VerificationStatus.PENDING, null);
        };
    }

    private void requireConfigured() {
        if (!stripe.configured()) {
            throw new ApiException(ErrorCode.PROVIDER_NOT_CONFIGURED, "Stripe is not configured");
        }
    }
}
