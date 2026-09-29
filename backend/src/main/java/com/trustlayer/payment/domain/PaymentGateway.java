package com.trustlayer.payment.domain;

public interface PaymentGateway {

    CheckoutSession createCheckout(CheckoutRequest request);

    GatewayEvent parseWebhook(String payload, String signature);
}
