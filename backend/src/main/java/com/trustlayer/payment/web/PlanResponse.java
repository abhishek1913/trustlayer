package com.trustlayer.payment.web;

import com.trustlayer.payment.domain.Plan;

public record PlanResponse(String code, String name, long amountCents, String currency, String interval) {

    static PlanResponse from(Plan plan) {
        return new PlanResponse(plan.getCode(), plan.getName(), plan.getAmountCents(), plan.getCurrency(), plan.getBillingInterval());
    }
}
