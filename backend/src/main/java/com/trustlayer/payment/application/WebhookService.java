package com.trustlayer.payment.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.trustlayer.payment.domain.GatewayEvent;
import com.trustlayer.payment.domain.PaymentAttempt;
import com.trustlayer.payment.domain.PaymentGateway;
import com.trustlayer.payment.domain.PaymentTransaction;
import com.trustlayer.payment.domain.Subscription;
import com.trustlayer.payment.domain.SubscriptionStatus;
import com.trustlayer.payment.infrastructure.PaymentAttemptRepository;
import com.trustlayer.payment.infrastructure.PaymentTransactionRepository;
import com.trustlayer.payment.infrastructure.PaymentWebhookEventRepository;
import com.trustlayer.payment.infrastructure.SubscriptionRepository;
import com.trustlayer.shared.event.DomainEvents;
import com.trustlayer.shared.event.EventType;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final String PROVIDER = "stripe";

    private final PaymentGateway gateway;
    private final PaymentWebhookEventRepository webhookEvents;
    private final PaymentTransactionRepository transactions;
    private final PaymentAttemptRepository attempts;
    private final SubscriptionRepository subscriptions;
    private final DomainEvents events;

    public WebhookService(PaymentGateway gateway, PaymentWebhookEventRepository webhookEvents,
                          PaymentTransactionRepository transactions, PaymentAttemptRepository attempts,
                          SubscriptionRepository subscriptions, DomainEvents events) {
        this.gateway = gateway;
        this.webhookEvents = webhookEvents;
        this.transactions = transactions;
        this.attempts = attempts;
        this.subscriptions = subscriptions;
        this.events = events;
    }

    @Transactional
    public void handle(String payload, String signature) {
        GatewayEvent event = gateway.parseWebhook(payload, signature);
        if (webhookEvents.insertIfAbsent(PROVIDER, event.id(), event.type(), Instant.now()) == 0) {
            log.info("Duplicate payment webhook skipped");
            return;
        }
        switch (event.type()) {
            case "checkout.session.completed" -> onCheckoutCompleted(event.object());
            case "checkout.session.expired" -> onCheckoutExpired(event.object());
            case "invoice.payment_failed" -> onPaymentFailed(event.object());
            case "customer.subscription.deleted" -> onSubscriptionDeleted(event.object());
            default -> log.info("Payment webhook type ignored: {}", event.type());
        }
    }

    private void onCheckoutCompleted(JsonNode session) {
        String paymentStatus = session.path("payment_status").asText();
        if (!paymentStatus.equals("paid") && !paymentStatus.equals("no_payment_required")) {
            return;
        }
        transactions.findByCheckoutSessionId(session.path("id").asText()).ifPresentOrElse(transaction -> {
            if (!transaction.isPending()) {
                return;
            }
            String stripeSubscriptionId = session.path("subscription").asText(null);
            Instant now = Instant.now();
            transaction.markSucceeded(stripeSubscriptionId);
            attempts.save(new PaymentAttempt(transaction.getId(), PaymentAttempt.Outcome.SUCCEEDED, null));
            events.publish(EventType.PAYMENT_SUCCEEDED, transaction.getId(),
                    Map.of("userId", transaction.getUserId().toString(), "paymentId", transaction.getId().toString()));
            activateSubscription(transaction, stripeSubscriptionId, now);
        }, () -> log.warn("Checkout completed webhook for unknown session"));
    }

    private void activateSubscription(PaymentTransaction transaction, String stripeSubscriptionId, Instant now) {
        if (stripeSubscriptionId == null
                || subscriptions.existsByUserIdAndStatus(transaction.getUserId(), SubscriptionStatus.ACTIVE)) {
            log.warn("Subscription not created for succeeded payment {}", transaction.getId());
            return;
        }
        Subscription subscription = subscriptions.save(new Subscription(
                transaction.getUserId(), transaction.getPlanId(), transaction.getId(), stripeSubscriptionId, now));
        events.publish(EventType.SUBSCRIPTION_ACTIVATED, subscription.getId(),
                Map.of("userId", transaction.getUserId().toString(), "subscriptionId", subscription.getId().toString()));
    }

    private void onCheckoutExpired(JsonNode session) {
        transactions.findByCheckoutSessionId(session.path("id").asText()).ifPresent(transaction -> {
            if (transaction.isPending()) {
                transaction.markExpired();
                attempts.save(new PaymentAttempt(transaction.getId(), PaymentAttempt.Outcome.EXPIRED, null));
            }
        });
    }

    private void onPaymentFailed(JsonNode invoice) {
        String subscriptionId = invoice.path("subscription").asText(null);
        if (subscriptionId == null) {
            subscriptionId = invoice.path("parent").path("subscription_details").path("subscription").asText(null);
        }
        if (subscriptionId == null) {
            return;
        }
        transactions.findFirstByStripeSubscriptionId(subscriptionId).ifPresentOrElse(transaction -> {
            attempts.save(new PaymentAttempt(transaction.getId(), PaymentAttempt.Outcome.FAILED, "invoice.payment_failed"));
            if (transaction.isPending()) {
                transaction.markFailed();
            }
            events.publish(EventType.PAYMENT_FAILED, transaction.getId(),
                    Map.of("userId", transaction.getUserId().toString(), "paymentId", transaction.getId().toString()));
        }, () -> log.warn("Payment failure webhook for unknown subscription"));
    }

    private void onSubscriptionDeleted(JsonNode stripeSubscription) {
        subscriptions.findByStripeSubscriptionId(stripeSubscription.path("id").asText()).ifPresent(subscription -> {
            if (subscription.isActive()) {
                subscription.cancel(Instant.now());
                events.publish(EventType.SUBSCRIPTION_CANCELLED, subscription.getId(),
                        Map.of("userId", subscription.getUserId().toString(), "subscriptionId", subscription.getId().toString()));
            }
        });
    }
}
