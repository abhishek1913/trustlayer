package com.trustlayer.admin.web;

import com.trustlayer.admin.application.AdminQueryService;
import com.trustlayer.admin.application.UserOverview;
import com.trustlayer.notification.api.DeliverySummary;
import com.trustlayer.notification.api.NotificationAdminQueries;
import com.trustlayer.payment.api.PaymentAdminQueries;
import com.trustlayer.payment.api.PaymentSummary;
import com.trustlayer.shared.web.PageResponse;
import com.trustlayer.user.api.UserDirectory;
import com.trustlayer.user.api.UserSummary;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
class AdminController {

    private final UserDirectory users;
    private final PaymentAdminQueries payments;
    private final NotificationAdminQueries notifications;
    private final AdminQueryService admin;

    AdminController(UserDirectory users, PaymentAdminQueries payments, NotificationAdminQueries notifications,
                    AdminQueryService admin) {
        this.users = users;
        this.payments = payments;
        this.notifications = notifications;
        this.admin = admin;
    }

    @GetMapping("/users")
    PageResponse<UserSummary> users(@RequestParam(required = false) String email,
                                    @RequestParam(required = false) Boolean emailVerified,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(users.search(email, emailVerified, PageResponse.pageable(page, size)), u -> u);
    }

    @GetMapping("/users/{id}/overview")
    UserOverview overview(@PathVariable UUID id) {
        return admin.overview(id);
    }

    @GetMapping("/payments")
    PageResponse<PaymentSummary> payments(@RequestParam(required = false) String status,
                                          @RequestParam(required = false) UUID userId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(payments.payments(status, userId, PageResponse.pageable(page, size)), p -> p);
    }

    @GetMapping("/notifications")
    PageResponse<DeliverySummary> notifications(@RequestParam(required = false) String status,
                                                @RequestParam(required = false) String channel,
                                                @RequestParam(required = false) UUID userId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(notifications.deliveries(status, channel, userId, PageResponse.pageable(page, size)), d -> d);
    }
}
