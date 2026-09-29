package com.trustlayer.verification.domain;

import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "verification_sessions")
public class VerificationSession extends AuditedEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false, updatable = false)
    private String provider;

    @Column(name = "provider_ref", nullable = false, updatable = false)
    private String providerRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    protected VerificationSession() {
    }

    public VerificationSession(UUID userId, String provider, String providerRef, Instant expiresAt) {
        this.userId = userId;
        this.provider = provider;
        this.providerRef = providerRef;
        this.expiresAt = expiresAt;
        this.status = VerificationStatus.PENDING;
    }

    public boolean isExpiredAt(Instant now) {
        return !status.isTerminal() && !expiresAt.isAfter(now);
    }

    public void markExpired() {
        status = VerificationStatus.EXPIRED;
    }

    public void markInProgress() {
        if (status == VerificationStatus.PENDING) {
            status = VerificationStatus.IN_PROGRESS;
        }
    }

    public void complete(VerificationStatus outcome) {
        if (outcome != VerificationStatus.VERIFIED && outcome != VerificationStatus.REJECTED) {
            throw new IllegalArgumentException("Outcome must be VERIFIED or REJECTED");
        }
        if (status.isTerminal()) {
            throw new IllegalStateException("Session already finished");
        }
        status = outcome;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderRef() {
        return providerRef;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
