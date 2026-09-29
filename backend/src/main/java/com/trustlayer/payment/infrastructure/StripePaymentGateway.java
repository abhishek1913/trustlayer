package com.trustlayer.payment.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.trustlayer.payment.domain.CheckoutRequest;
import com.trustlayer.payment.domain.CheckoutSession;
import com.trustlayer.payment.domain.GatewayEvent;
import com.trustlayer.payment.domain.PaymentGateway;
import com.trustlayer.payment.domain.Plan;
import com.trustlayer.shared.config.StripeProperties;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class StripePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentGateway.class);

    private final StripeProperties stripe;
    private final PaymentProperties properties;
    private final ObjectMapper objectMapper;

    StripePaymentGateway(StripeProperties stripe, PaymentProperties properties, ObjectMapper objectMapper) {
        this.stripe = stripe;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public CheckoutSession createCheckout(CheckoutRequest request) {
        if (!stripe.configured()) {
            throw new ApiException(ErrorCode.PROVIDER_NOT_CONFIGURED, "Stripe is not configured");
        }
        String userId = request.userId().toString();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setClientReferenceId(request.transactionId().toString())
                .setCustomerEmail(request.userEmail())
                .setSuccessUrl(properties.successUrl())
                .setCancelUrl(properties.cancelUrl())
                .putMetadata("transaction_id", request.transactionId().toString())
                .putMetadata("user_id", userId)
                .putMetadata("plan_code", request.plan().getCode())
                .setSubscriptionData(SessionCreateParams.SubscriptionData.builder().putMetadata("user_id", userId).build())
                .addLineItem(lineItem(request.plan()))
                .build();
        RequestOptions options = RequestOptions.builder()
                .setIdempotencyKey("checkout-" + userId + "-" + request.idempotencyKey())
                .build();
        try {
            Session session = Session.create(params, options);
            return new CheckoutSession(session.getId(), session.getUrl());
        } catch (StripeException e) {
            log.warn("Stripe checkout creation failed: {}", e.getClass().getSimpleName());
            throw new ApiException(ErrorCode.PROVIDER_ERROR, "Payment provider is unavailable");
        }
    }

    @Override
    public GatewayEvent parseWebhook(String payload, String signature) {
        String secret = properties.webhookSecret();
        if (secret == null || secret.isBlank()) {
            throw new ApiException(ErrorCode.PROVIDER_NOT_CONFIGURED, "Payment webhook secret is not configured");
        }
        try {
            Webhook.constructEvent(payload, signature, secret);
        } catch (SignatureVerificationException | RuntimeException e) {
            throw new ApiException(ErrorCode.INVALID_WEBHOOK_SIGNATURE, "Invalid webhook signature");
        }
        try {
            JsonNode event = objectMapper.readTree(payload);
            return new GatewayEvent(event.path("id").asText(), event.path("type").asText(), event.path("data").path("object"));
        } catch (Exception e) {
            throw new ApiException(ErrorCode.MALFORMED_REQUEST, "Malformed webhook payload");
        }
    }

    private SessionCreateParams.LineItem lineItem(Plan plan) {
        SessionCreateParams.LineItem.Builder item = SessionCreateParams.LineItem.builder().setQuantity(1L);
        if (plan.getStripePriceId() != null) {
            return item.setPrice(plan.getStripePriceId()).build();
        }
        return item.setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(plan.getCurrency())
                .setUnitAmount(plan.getAmountCents())
                .setRecurring(SessionCreateParams.LineItem.PriceData.Recurring.builder()
                        .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                        .build())
                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(plan.getName()).build())
                .build()).build();
    }
}
