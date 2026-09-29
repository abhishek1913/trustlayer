package com.trustlayer.payment.application;

import com.trustlayer.payment.domain.CheckoutRequest;
import com.trustlayer.payment.domain.CheckoutSession;
import com.trustlayer.payment.domain.PaymentAttempt;
import com.trustlayer.payment.domain.PaymentGateway;
import com.trustlayer.payment.domain.PaymentTransaction;
import com.trustlayer.payment.domain.Plan;
import com.trustlayer.payment.domain.SubscriptionStatus;
import com.trustlayer.payment.infrastructure.IdempotencyStore;
import com.trustlayer.payment.infrastructure.PaymentAttemptRepository;
import com.trustlayer.payment.infrastructure.PaymentTransactionRepository;
import com.trustlayer.payment.infrastructure.PlanRepository;
import com.trustlayer.payment.infrastructure.SubscriptionRepository;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.user.api.UserDirectory;
import com.trustlayer.user.api.UserSummary;
import com.trustlayer.verification.api.IdentityStatusQuery;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class CheckoutService {

    private final PlanRepository plans;
    private final PaymentTransactionRepository transactions;
    private final PaymentAttemptRepository attempts;
    private final SubscriptionRepository subscriptions;
    private final IdempotencyStore idempotency;
    private final PaymentGateway gateway;
    private final IdentityStatusQuery identity;
    private final UserDirectory users;
    private final TransactionTemplate tx;

    public CheckoutService(PlanRepository plans, PaymentTransactionRepository transactions,
                           PaymentAttemptRepository attempts, SubscriptionRepository subscriptions,
                           IdempotencyStore idempotency, PaymentGateway gateway, IdentityStatusQuery identity,
                           UserDirectory users, TransactionTemplate tx) {
        this.plans = plans;
        this.transactions = transactions;
        this.attempts = attempts;
        this.subscriptions = subscriptions;
        this.idempotency = idempotency;
        this.gateway = gateway;
        this.identity = identity;
        this.users = users;
        this.tx = tx;
    }

    public CheckoutResult checkout(UUID userId, String key, String planCode) {
        Plan plan = plans.findByCodeAndActiveTrue(planCode)
                .orElseThrow(() -> new ApiException(ErrorCode.PLAN_NOT_FOUND, "Plan not found"));
        String requestHash = PaymentHashes.sha256(planCode);
        var existing = idempotency.find(userId, key);
        if (existing.isPresent()) {
            return replay(existing.get(), requestHash);
        }
        if (!identity.isVerified(userId)) {
            throw new ApiException(ErrorCode.IDENTITY_NOT_VERIFIED, "Identity verification is required before payment");
        }
        if (subscriptions.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)) {
            throw new ApiException(ErrorCode.ALREADY_SUBSCRIBED, "User already has an active subscription");
        }
        UserSummary user = users.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        if (!tx.execute(status -> idempotency.claim(userId, key, requestHash))) {
            return replay(idempotency.find(userId, key).orElseThrow(), requestHash);
        }
        try {
            UUID transactionId = UUID.randomUUID();
            CheckoutSession session = gateway.createCheckout(new CheckoutRequest(transactionId, userId, user.email(), plan, key));
            return tx.execute(status -> {
                PaymentTransaction transaction = transactions.saveAndFlush(
                        new PaymentTransaction(transactionId, userId, plan, session.id(), session.url()));
                attempts.save(new PaymentAttempt(transaction.getId(), PaymentAttempt.Outcome.STARTED, null));
                idempotency.attach(userId, key, transaction.getId());
                return result(transaction, plan.getCode(), false);
            });
        } catch (RuntimeException e) {
            tx.executeWithoutResult(status -> idempotency.release(userId, key));
            throw e;
        }
    }

    private CheckoutResult replay(IdempotencyStore.Claim claim, String requestHash) {
        if (!claim.requestHash().equals(requestHash)) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_KEY_REUSED, "Idempotency-Key was already used with a different request");
        }
        if (claim.transactionId() == null) {
            throw new ApiException(ErrorCode.REQUEST_IN_PROGRESS, "A request with this Idempotency-Key is still in progress");
        }
        PaymentTransaction transaction = transactions.findById(claim.transactionId()).orElseThrow();
        String planCode = plans.findById(transaction.getPlanId()).map(Plan::getCode).orElse(null);
        return result(transaction, planCode, true);
    }

    private CheckoutResult result(PaymentTransaction transaction, String planCode, boolean replayed) {
        return new CheckoutResult(transaction.getId(), transaction.getStatus().name(), transaction.getCheckoutUrl(), planCode, replayed);
    }
}
