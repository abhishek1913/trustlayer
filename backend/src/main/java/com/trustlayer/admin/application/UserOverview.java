package com.trustlayer.admin.application;

import com.trustlayer.access.api.AccessSummary;
import com.trustlayer.notification.api.DeliverySummary;
import com.trustlayer.payment.api.PaymentSummary;
import com.trustlayer.payment.api.SubscriptionSummary;
import com.trustlayer.user.api.UserSummary;
import com.trustlayer.verification.api.VerificationSummary;
import java.util.List;

public record UserOverview(
        UserSummary user,
        VerificationSummary verification,
        List<PaymentSummary> payments,
        SubscriptionSummary subscription,
        AccessSummary access,
        List<DeliverySummary> notifications) {
}
