package com.trustlayer.payment.domain;

import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription extends AuditedEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false, updatable = false)
    private UUID planId;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "stripe_subscription_id", nullable = false, updatable = false)
    private String stripeSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    protected Subscription() {
    }

    public Subscription(UUID userId, UUID planId, UUID transactionId, String stripeSubscriptionId, Instant startedAt) {
        this.userId = userId;
        this.planId = planId;
        this.transactionId = transactionId;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.startedAt = startedAt;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public void cancel(Instant now) {
        status = SubscriptionStatus.CANCELLED;
        cancelledAt = now;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }
}
