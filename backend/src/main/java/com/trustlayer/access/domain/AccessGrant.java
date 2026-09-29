package com.trustlayer.access.domain;

import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "access_grants")
public class AccessGrant extends AuditedEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "identity_verified", nullable = false)
    private boolean identityVerified;

    @Column(name = "payment_succeeded", nullable = false)
    private boolean paymentSucceeded;

    @Column(name = "subscription_active", nullable = false)
    private boolean subscriptionActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessState state;

    protected AccessGrant() {
    }

    public void record(AccessFact fact, boolean value) {
        switch (fact) {
            case EMAIL_VERIFIED -> emailVerified = value;
            case IDENTITY_VERIFIED -> identityVerified = value;
            case PAYMENT_SUCCEEDED -> paymentSucceeded = value;
            case SUBSCRIPTION_ACTIVE -> subscriptionActive = value;
        }
    }

    public boolean shouldGrant() {
        return emailVerified && identityVerified && paymentSucceeded && subscriptionActive;
    }

    public boolean recompute() {
        AccessState next = shouldGrant() ? AccessState.GRANTED : AccessState.BLOCKED;
        boolean changed = next != state;
        state = next;
        return changed;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isIdentityVerified() {
        return identityVerified;
    }

    public boolean isPaymentSucceeded() {
        return paymentSucceeded;
    }

    public boolean isSubscriptionActive() {
        return subscriptionActive;
    }

    public AccessState getState() {
        return state;
    }
}
