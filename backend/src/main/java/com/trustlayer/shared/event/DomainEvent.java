package com.trustlayer.shared.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DomainEvent(
        UUID eventId,
        EventType eventType,
        int eventVersion,
        Instant occurredAt,
        UUID aggregateId,
        Map<String, Object> payload) {

    public UUID userId() {
        return UUID.fromString(payload.get("userId").toString());
    }

    @Override
    public String toString() {
        return "DomainEvent[" + eventType + " " + eventId + "]";
    }
}
