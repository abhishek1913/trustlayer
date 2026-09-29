package com.trustlayer.payment.domain;

import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt extends AuditedEntity {

    public enum Outcome {
        STARTED,
        SUCCEEDED,
        FAILED,
        EXPIRED
    }

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Outcome outcome;

    @Column(updatable = false)
    private String detail;

    protected PaymentAttempt() {
    }

    public PaymentAttempt(UUID transactionId, Outcome outcome, String detail) {
        this.transactionId = transactionId;
        this.outcome = outcome;
        this.detail = detail;
    }
}
