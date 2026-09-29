package com.trustlayer.verification.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.verification.domain.IdentityVerificationProvider;
import com.trustlayer.verification.domain.ProviderOutcome;
import com.trustlayer.verification.domain.ProviderSession;
import com.trustlayer.verification.domain.ProviderWebhookEvent;
import com.trustlayer.verification.domain.VerificationStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "trustlayer.verification.provider", havingValue = "mock", matchIfMissing = true)
class MockIdentityProvider implements IdentityVerificationProvider {

    private final VerificationProperties properties;
    private final ObjectMapper objectMapper;

    MockIdentityProvider(VerificationProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String name() {
        return "mock";
    }

    @Override
    public boolean isMock() {
        return true;
    }

    @Override
    public String signatureHeaderName() {
        return "X-Mock-Signature";
    }

    @Override
    public ProviderSession createSession(UUID userId) {
        return new ProviderSession("mock_" + UUID.randomUUID(), null);
    }

    @Override
    public Optional<ProviderOutcome> getResult(String providerRef) {
        return Optional.empty();
    }

    @Override
    public ProviderWebhookEvent parseWebhook(String payload, String signature) {
        String secret = properties.mockWebhookSecret();
        if (secret == null || secret.isBlank() || signature == null || !matches(secret, payload, signature)) {
            throw new ApiException(ErrorCode.INVALID_WEBHOOK_SIGNATURE, "Invalid webhook signature");
        }
        try {
            JsonNode json = objectMapper.readTree(payload);
            VerificationStatus status = VerificationStatus.valueOf(json.path("status").asText());
            return new ProviderWebhookEvent(
                    json.path("eventId").asText(),
                    "mock.verification." + status.name().toLowerCase(),
                    json.path("providerRef").asText(null),
                    new ProviderOutcome(status, json.path("reason").asText(null)));
        } catch (Exception e) {
            throw new ApiException(ErrorCode.MALFORMED_REQUEST, "Malformed webhook payload");
        }
    }

    private boolean matches(String secret, String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
