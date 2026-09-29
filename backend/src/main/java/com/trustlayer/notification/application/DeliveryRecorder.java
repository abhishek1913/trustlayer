package com.trustlayer.notification.application;

import com.trustlayer.notification.domain.Channel;
import com.trustlayer.notification.domain.NotificationDelivery;
import com.trustlayer.notification.domain.NotificationEvent;
import com.trustlayer.notification.infrastructure.NotificationDeliveryRepository;
import com.trustlayer.notification.infrastructure.NotificationEventRepository;
import com.trustlayer.shared.event.DomainEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeliveryRecorder {

    private final NotificationEventRepository events;
    private final NotificationDeliveryRepository deliveries;

    DeliveryRecorder(NotificationEventRepository events, NotificationDeliveryRepository deliveries) {
        this.events = events;
        this.deliveries = deliveries;
    }

    @Transactional
    List<NotificationDelivery> open(DomainEvent event, List<Channel> channels) {
        if (events.existsByEventId(event.eventId())) {
            return List.of();
        }
        NotificationEvent record = events.saveAndFlush(new NotificationEvent(event.eventId(), event.eventType(), event.userId()));
        return channels.stream()
                .map(channel -> deliveries.save(new NotificationDelivery(record.getId(), event.userId(), event.eventType(), channel)))
                .toList();
    }

    @Transactional
    void sent(UUID deliveryId, int attempt) {
        deliveries.findById(deliveryId).ifPresent(d -> d.markSent(attempt));
    }

    @Transactional
    void failed(UUID deliveryId, int attempt, boolean giveUp, String error) {
        deliveries.findById(deliveryId).ifPresent(d -> d.markFailedAttempt(attempt, giveUp, error));
    }
}
