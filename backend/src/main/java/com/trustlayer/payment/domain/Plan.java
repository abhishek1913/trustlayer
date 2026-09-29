package com.trustlayer.payment.domain;

import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "plans")
public class Plan extends AuditedEntity {

    @Column(nullable = false, updatable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false)
    private String currency;

    @Column(name = "billing_interval", nullable = false)
    private String billingInterval;

    @Column(nullable = false)
    private boolean active;

    protected Plan() {
    }

    public void assignStripePrice(String priceId) {
        this.stripePriceId = priceId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getStripePriceId() {
        return stripePriceId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBillingInterval() {
        return billingInterval;
    }

    public boolean isActive() {
        return active;
    }
}
