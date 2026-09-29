package com.trustlayer.payment.domain;

import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends AuditedEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "plan_id", nullable = false, updatable = false)
    private UUID planId;

    @Column(name = "checkout_session_id", nullable = false, updatable = false)
    private String checkoutSessionId;

    @Column(name = "checkout_url", nullable = false, updatable = false)
    private String checkoutUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "amount_cents", nullable = false, updatable = false)
    private long amountCents;

    @Column(nullable = false, updatable = false)
    private String currency;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(UUID id, UUID userId, Plan plan, String checkoutSessionId, String checkoutUrl) {
        this.userId = userId;
        this.planId = plan.getId();
        this.checkoutSessionId = checkoutSessionId;
        this.checkoutUrl = checkoutUrl;
        this.amountCents = plan.getAmountCents();
        this.currency = plan.getCurrency();
        this.status = PaymentStatus.PENDING;
        assignId(id);
    }

    public boolean isPending() {
        return status == PaymentStatus.PENDING;
    }

    public void markSucceeded(String subscriptionId) {
        status = PaymentStatus.SUCCEEDED;
        stripeSubscriptionId = subscriptionId;
    }

    public void markFailed() {
        status = PaymentStatus.FAILED;
    }

    public void markExpired() {
        status = PaymentStatus.EXPIRED;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }
}
