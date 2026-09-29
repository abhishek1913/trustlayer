package com.trustlayer.notification.infrastructure;

import com.trustlayer.notification.domain.NotificationEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {

    boolean existsByEventId(UUID eventId);
}
