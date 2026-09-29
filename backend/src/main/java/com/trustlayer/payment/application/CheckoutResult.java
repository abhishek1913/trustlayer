package com.trustlayer.payment.application;

import java.util.UUID;

public record CheckoutResult(UUID paymentId, String status, String checkoutUrl, String planCode, boolean replayed) {
}
