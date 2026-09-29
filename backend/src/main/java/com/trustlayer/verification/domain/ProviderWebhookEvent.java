package com.trustlayer.verification.domain;

public record ProviderWebhookEvent(String eventId, String eventType, String providerRef, ProviderOutcome outcome) {
}
