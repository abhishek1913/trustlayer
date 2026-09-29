package com.trustlayer.payment.web;

import com.trustlayer.payment.api.PaymentSummary;
import com.trustlayer.payment.api.SubscriptionSummary;
import com.trustlayer.payment.application.CheckoutResult;
import com.trustlayer.payment.application.CheckoutService;
import com.trustlayer.payment.application.PaymentQueryService;
import com.trustlayer.payment.application.WebhookService;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.shared.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PaymentController {

    private final CheckoutService checkout;
    private final PaymentQueryService queries;
    private final WebhookService webhooks;

    PaymentController(CheckoutService checkout, PaymentQueryService queries, WebhookService webhooks) {
        this.checkout = checkout;
        this.queries = queries;
        this.webhooks = webhooks;
    }

    @GetMapping("/api/v1/plans")
    List<PlanResponse> plans() {
        return queries.activePlans().stream().map(PlanResponse::from).toList();
    }

    @PostMapping("/api/v1/payments/checkout")
    ResponseEntity<CheckoutResult> checkout(@AuthenticationPrincipal AuthenticatedUser principal,
                                            @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                            @Valid @RequestBody CheckoutRequestBody body) {
        if (key == null || key.isBlank() || key.length() > 100) {
            throw new ApiException(ErrorCode.IDEMPOTENCY_KEY_REQUIRED, "Idempotency-Key header is required (max 100 characters)");
        }
        CheckoutResult result = checkout.checkout(principal.userId(), key.trim(), body.planCode());
        HttpStatus status = result.replayed() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).header("Idempotent-Replayed", String.valueOf(result.replayed())).body(result);
    }

    @GetMapping("/api/v1/payments/{id}")
    PaymentSummary payment(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        return queries.payment(principal, id);
    }

    @GetMapping("/api/v1/subscriptions/me")
    SubscriptionSummary subscription(@AuthenticationPrincipal AuthenticatedUser principal) {
        return queries.mySubscription(principal.userId());
    }

    @PostMapping("/api/v1/payments/webhook")
    void webhook(@RequestBody byte[] body, @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
        webhooks.handle(new String(body, StandardCharsets.UTF_8), signature);
    }
}
