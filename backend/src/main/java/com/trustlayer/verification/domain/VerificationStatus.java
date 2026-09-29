package com.trustlayer.verification.domain;

public enum VerificationStatus {
    PENDING,
    IN_PROGRESS,
    VERIFIED,
    REJECTED,
    EXPIRED;

    public boolean isTerminal() {
        return this == VERIFIED || this == REJECTED || this == EXPIRED;
    }
}
