package com.trustlayer.admin.application;

import com.trustlayer.access.api.AccessAdminQueries;
import com.trustlayer.notification.api.NotificationAdminQueries;
import com.trustlayer.payment.api.PaymentAdminQueries;
import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.user.api.UserDirectory;
import com.trustlayer.user.api.UserSummary;
import com.trustlayer.verification.api.IdentityStatusQuery;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AdminQueryService {

    private static final int RECENT_NOTIFICATIONS = 20;

    private final UserDirectory users;
    private final IdentityStatusQuery identity;
    private final PaymentAdminQueries payments;
    private final AccessAdminQueries access;
    private final NotificationAdminQueries notifications;

    public AdminQueryService(UserDirectory users, IdentityStatusQuery identity, PaymentAdminQueries payments,
                             AccessAdminQueries access, NotificationAdminQueries notifications) {
        this.users = users;
        this.identity = identity;
        this.payments = payments;
        this.access = access;
        this.notifications = notifications;
    }

    public UserOverview overview(UUID userId) {
        UserSummary user = users.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        return new UserOverview(
                user,
                identity.latestFor(userId).orElse(null),
                payments.paymentsFor(userId),
                payments.latestSubscriptionFor(userId).orElse(null),
                access.accessFor(userId).orElse(null),
                notifications.recentFor(userId, RECENT_NOTIFICATIONS));
    }
}
