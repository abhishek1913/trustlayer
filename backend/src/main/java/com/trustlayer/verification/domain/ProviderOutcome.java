package com.trustlayer.verification.domain;

public record ProviderOutcome(VerificationStatus status, String reason) {
}
