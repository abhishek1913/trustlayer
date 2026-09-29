package com.trustlayer.notification.application;

import com.trustlayer.notification.api.DeliverySummary;
import com.trustlayer.notification.api.NotificationAdminQueries;
import com.trustlayer.notification.domain.Channel;
import com.trustlayer.notification.domain.DeliveryStatus;
import com.trustlayer.notification.domain.NotificationDelivery;
import com.trustlayer.notification.infrastructure.NotificationDeliveryRepository;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class NotificationQueryService implements NotificationAdminQueries {

    private final NotificationDeliveryRepository deliveries;

    NotificationQueryService(NotificationDeliveryRepository deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public Page<DeliverySummary> deliveries(String status, String channel, UUID userId, Pageable pageable) {
        Specification<NotificationDelivery> spec = Specification.where(null);
        if (status != null && !status.isBlank()) {
            DeliveryStatus parsed = parse(DeliveryStatus.class, status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parsed));
        }
        if (channel != null && !channel.isBlank()) {
            Channel parsed = parse(Channel.class, channel);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("channel"), parsed));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        return deliveries.findAll(spec, pageable).map(NotificationQueryService::toSummary);
    }

    @Override
    public List<DeliverySummary> recentFor(UUID userId, int limit) {
        return deliveries.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(NotificationQueryService::toSummary).toList();
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Unknown " + type.getSimpleName().toLowerCase() + " value");
        }
    }

    private static DeliverySummary toSummary(NotificationDelivery d) {
        return new DeliverySummary(d.getId(), d.getUserId(), d.getEventType().name(), d.getChannel().name(),
                d.getStatus().name(), d.getAttempts(), d.getLastError(), d.getSentAt(), d.getCreatedAt());
    }
}
