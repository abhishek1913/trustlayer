package com.trustlayer.access.application;

import com.trustlayer.access.domain.AccessFact;
import com.trustlayer.shared.event.DomainEvent;
import com.trustlayer.shared.event.DomainEventListener;
import org.springframework.stereotype.Component;

@Component
class AccessEventListener {

    private final AccessService access;

    AccessEventListener(AccessService access) {
        this.access = access;
    }

    @DomainEventListener
    void on(DomainEvent event) {
        switch (event.eventType()) {
            case EMAIL_VERIFIED -> access.record(event.userId(), AccessFact.EMAIL_VERIFIED, true);
            case IDENTITY_VERIFIED -> access.record(event.userId(), AccessFact.IDENTITY_VERIFIED, true);
            case PAYMENT_SUCCEEDED -> access.record(event.userId(), AccessFact.PAYMENT_SUCCEEDED, true);
            case SUBSCRIPTION_ACTIVATED -> access.record(event.userId(), AccessFact.SUBSCRIPTION_ACTIVE, true);
            case SUBSCRIPTION_CANCELLED -> access.record(event.userId(), AccessFact.SUBSCRIPTION_ACTIVE, false);
            default -> {
            }
        }
    }
}
