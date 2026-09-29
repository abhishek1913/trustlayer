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
@Table(name = "verification_results")
public class VerificationResult extends AuditedEntity {

    @Column(name = "session_id", nullable = false, updatable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private VerificationStatus outcome;

    @Column(updatable = false)
    private String reason;

    @Column(name = "decided_at", nullable = false, updatable = false)
    private Instant decidedAt;

    protected VerificationResult() {
    }

    public VerificationResult(UUID sessionId, VerificationStatus outcome, String reason, Instant decidedAt) {
        this.sessionId = sessionId;
        this.outcome = outcome;
        this.reason = reason;
        this.decidedAt = decidedAt;
    }
}
