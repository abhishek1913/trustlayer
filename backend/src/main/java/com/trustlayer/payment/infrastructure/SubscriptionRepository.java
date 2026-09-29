package com.trustlayer.payment.infrastructure;

import com.trustlayer.payment.domain.Subscription;
import com.trustlayer.payment.domain.SubscriptionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    boolean existsByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<Subscription> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
