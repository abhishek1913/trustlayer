package com.trustlayer.notification.infrastructure;

import com.trustlayer.notification.domain.NotificationDelivery;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID>, JpaSpecificationExecutor<NotificationDelivery> {

    List<NotificationDelivery> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
