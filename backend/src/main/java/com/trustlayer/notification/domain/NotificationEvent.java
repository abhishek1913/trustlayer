package com.trustlayer.notification.domain;

import com.trustlayer.shared.event.EventType;
import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "notification_events")
public class NotificationEvent extends AuditedEntity {

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private EventType eventType;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    protected NotificationEvent() {
    }

    public NotificationEvent(UUID eventId, EventType eventType, UUID userId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
    }
}
