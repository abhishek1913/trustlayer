package com.trustlayer.payment.infrastructure;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class PlanPriceSynchronizer implements ApplicationRunner {

    private final PaymentProperties properties;
    private final PlanRepository plans;

    PlanPriceSynchronizer(PaymentProperties properties, PlanRepository plans) {
        this.properties = properties;
        this.plans = plans;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (properties.priceIds() == null) {
            return;
        }
        properties.priceIds().forEach((code, priceId) -> {
            if (priceId != null && !priceId.isBlank()) {
                plans.findByCodeAndActiveTrue(code).ifPresent(plan -> plan.assignStripePrice(priceId));
            }
        });
    }
}
