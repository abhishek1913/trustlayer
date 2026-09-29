package com.trustlayer.notification.domain;

import com.trustlayer.shared.event.EventType;
import com.trustlayer.shared.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_deliveries")
public class NotificationDelivery extends AuditedEntity {

    @Column(name = "notification_event_id", nullable = false, updatable = false)
    private UUID notificationEventId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "sent_at")
    private Instant sentAt;

    protected NotificationDelivery() {
    }

    public NotificationDelivery(UUID notificationEventId, UUID userId, EventType eventType, Channel channel) {
        this.notificationEventId = notificationEventId;
        this.userId = userId;
        this.eventType = eventType;
        this.channel = channel;
        this.status = DeliveryStatus.PENDING;
    }

    public void markSent(int attempt) {
        attempts = attempt;
        status = DeliveryStatus.SENT;
        lastError = null;
        sentAt = Instant.now();
    }

    public void markFailedAttempt(int attempt, boolean giveUp, String error) {
        attempts = attempt;
        status = giveUp ? DeliveryStatus.FAILED : DeliveryStatus.RETRYING;
        lastError = error;
    }

    public UUID getUserId() {
        return userId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Channel getChannel() {
        return channel;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
