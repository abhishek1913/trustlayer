package com.trustlayer.payment.infrastructure;

import com.trustlayer.payment.domain.PaymentTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID>, JpaSpecificationExecutor<PaymentTransaction> {

    Optional<PaymentTransaction> findByCheckoutSessionId(String checkoutSessionId);

    Optional<PaymentTransaction> findFirstByStripeSubscriptionId(String stripeSubscriptionId);

    List<PaymentTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
