package com.trustlayer.access.application;

import com.trustlayer.access.api.AccessAdminQueries;
import com.trustlayer.access.api.AccessSummary;
import com.trustlayer.access.domain.AccessFact;
import com.trustlayer.access.domain.AccessGrant;
import com.trustlayer.access.domain.AccessState;
import com.trustlayer.access.infrastructure.AccessGrantInitializer;
import com.trustlayer.access.infrastructure.AccessGrantRepository;
import com.trustlayer.shared.event.DomainEvents;
import com.trustlayer.shared.event.EventType;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccessService implements AccessAdminQueries {

    private final AccessGrantRepository grants;
    private final AccessGrantInitializer initializer;
    private final DomainEvents events;

    public AccessService(AccessGrantRepository grants, AccessGrantInitializer initializer, DomainEvents events) {
        this.grants = grants;
        this.initializer = initializer;
        this.events = events;
    }

    @Transactional
    public AccessSummary record(UUID userId, AccessFact fact, boolean value) {
        AccessGrant grant = lock(userId);
        grant.record(fact, value);
        return recompute(grant);
    }

    @Transactional
    public AccessSummary evaluate(UUID userId) {
        return recompute(lock(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AccessSummary> accessFor(UUID userId) {
        return grants.findByUserId(userId).map(AccessService::toSummary);
    }

    @Transactional(readOnly = true)
    public AccessSummary currentFor(UUID userId) {
        return accessFor(userId).orElseGet(() ->
                new AccessSummary(AccessState.BLOCKED.name(), false, false, false, false, Instant.now()));
    }

    private AccessGrant lock(UUID userId) {
        initializer.ensureExists(userId);
        return grants.lockByUserId(userId).orElseThrow();
    }

    private AccessSummary recompute(AccessGrant grant) {
        if (grant.recompute()) {
            EventType type = grant.getState() == AccessState.GRANTED ? EventType.ACCESS_GRANTED : EventType.ACCESS_BLOCKED;
            events.publish(type, grant.getId(), Map.of("userId", grant.getUserId().toString()));
        }
        return toSummary(grant);
    }

    private static AccessSummary toSummary(AccessGrant g) {
        return new AccessSummary(g.getState().name(), g.isEmailVerified(), g.isIdentityVerified(),
                g.isPaymentSucceeded(), g.isSubscriptionActive(), g.getUpdatedAt());
    }
}
