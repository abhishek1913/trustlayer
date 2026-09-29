package com.trustlayer.notification.api;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationAdminQueries {

    Page<DeliverySummary> deliveries(String status, String channel, UUID userId, Pageable pageable);

    List<DeliverySummary> recentFor(UUID userId, int limit);
}
