package com.trustlayer.notification.application;

import com.trustlayer.shared.event.DomainEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class NotificationEventListener {

    private final NotificationService notifications;

    NotificationEventListener(NotificationService notifications) {
        this.notifications = notifications;
    }

    @Async("notificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    void on(DomainEvent event) {
        notifications.handle(event);
    }
}
