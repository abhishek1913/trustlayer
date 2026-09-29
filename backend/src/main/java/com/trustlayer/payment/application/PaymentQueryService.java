package com.trustlayer.payment.application;

import com.trustlayer.payment.api.PaymentAdminQueries;
import com.trustlayer.payment.api.PaymentSummary;
import com.trustlayer.payment.api.SubscriptionSummary;
import com.trustlayer.payment.domain.PaymentStatus;
import com.trustlayer.payment.domain.PaymentTransaction;
import com.trustlayer.payment.domain.Plan;
import com.trustlayer.payment.domain.Subscription;
import com.trustlayer.payment.infrastructure.PaymentTransactionRepository;
import com.trustlayer.payment.infrastructure.PlanRepository;
import com.trustlayer.payment.infrastructure.SubscriptionRepository;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.shared.security.AuthenticatedUser;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentQueryService implements PaymentAdminQueries {

    private final PaymentTransactionRepository transactions;
    private final SubscriptionRepository subscriptions;
    private final PlanRepository plans;

    public PaymentQueryService(PaymentTransactionRepository transactions, SubscriptionRepository subscriptions,
                               PlanRepository plans) {
        this.transactions = transactions;
        this.subscriptions = subscriptions;
        this.plans = plans;
    }

    public List<Plan> activePlans() {
        return plans.findByActiveTrueOrderByAmountCentsAsc();
    }

    public PaymentSummary payment(AuthenticatedUser caller, UUID id) {
        PaymentTransaction transaction = transactions.findById(id)
                .filter(t -> caller.isAdmin() || t.getUserId().equals(caller.userId()))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Payment not found"));
        return toSummary(transaction, planCodes());
    }

    public SubscriptionSummary mySubscription(UUID userId) {
        return latestSubscriptionFor(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "No subscription found"));
    }

    @Override
    public Page<PaymentSummary> payments(String status, UUID userId, Pageable pageable) {
        Specification<PaymentTransaction> spec = Specification.where(null);
        if (status != null && !status.isBlank()) {
            PaymentStatus parsed = parseStatus(status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), parsed));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        Map<UUID, String> codes = planCodes();
        return transactions.findAll(spec, pageable).map(t -> toSummary(t, codes));
    }

    @Override
    public List<PaymentSummary> paymentsFor(UUID userId) {
        Map<UUID, String> codes = planCodes();
        return transactions.findByUserIdOrderByCreatedAtDesc(userId).stream().map(t -> toSummary(t, codes)).toList();
    }

    @Override
    public Optional<SubscriptionSummary> latestSubscriptionFor(UUID userId) {
        Map<UUID, String> codes = planCodes();
        return subscriptions.findFirstByUserIdOrderByCreatedAtDesc(userId).map(s -> toSummary(s, codes));
    }

    private PaymentStatus parseStatus(String status) {
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Unknown payment status");
        }
    }

    private Map<UUID, String> planCodes() {
        return plans.findAll().stream().collect(Collectors.toMap(Plan::getId, Plan::getCode, (a, b) -> a, java.util.HashMap::new));
    }

    private PaymentSummary toSummary(PaymentTransaction t, Map<UUID, String> codes) {
        return new PaymentSummary(t.getId(), t.getUserId(), codes.get(t.getPlanId()), t.getStatus().name(),
                t.getAmountCents(), t.getCurrency(), t.getCreatedAt());
    }

    private SubscriptionSummary toSummary(Subscription s, Map<UUID, String> codes) {
        return new SubscriptionSummary(s.getId(), s.getUserId(), codes.get(s.getPlanId()), s.getStatus().name(),
                s.getStartedAt(), s.getCancelledAt());
    }
}
