package com.trustlayer.payment.infrastructure;

import com.trustlayer.payment.domain.PaymentAttempt;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {
}
