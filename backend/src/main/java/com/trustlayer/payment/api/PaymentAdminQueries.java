package com.trustlayer.payment.api;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentAdminQueries {

    Page<PaymentSummary> payments(String status, UUID userId, Pageable pageable);

    List<PaymentSummary> paymentsFor(UUID userId);

    Optional<SubscriptionSummary> latestSubscriptionFor(UUID userId);
}
