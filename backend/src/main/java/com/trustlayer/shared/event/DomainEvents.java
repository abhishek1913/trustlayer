package com.trustlayer.shared.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEvents {

    private static final int CURRENT_VERSION = 1;

    private final ApplicationEventPublisher publisher;

    public DomainEvents(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(EventType type, UUID aggregateId, Map<String, Object> payload) {
        publisher.publishEvent(new DomainEvent(UUID.randomUUID(), type, CURRENT_VERSION, Instant.now(), aggregateId, payload));
    }
}
