package com.trustlayer.notification.application;

import com.trustlayer.notification.infrastructure.NotificationProperties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class DeliveryDispatcher {

    private static final Logger log = LoggerFactory.getLogger(DeliveryDispatcher.class);

    private final DeliveryRecorder recorder;
    private final NotificationProperties properties;

    DeliveryDispatcher(DeliveryRecorder recorder, NotificationProperties properties) {
        this.recorder = recorder;
        this.properties = properties;
    }

    void dispatch(UUID deliveryId, Runnable send) {
        for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
            try {
                send.run();
                recorder.sent(deliveryId, attempt);
                return;
            } catch (RuntimeException e) {
                boolean giveUp = attempt == properties.maxAttempts();
                recorder.failed(deliveryId, attempt, giveUp, e.getClass().getSimpleName());
                log.warn("Notification delivery attempt {} failed: {}", attempt, e.getClass().getSimpleName());
                if (giveUp || !pause(attempt)) {
                    if (!giveUp) {
                        recorder.failed(deliveryId, attempt, true, "Interrupted");
                    }
                    return;
                }
            }
        }
    }

    private boolean pause(int attempt) {
        try {
            Thread.sleep(properties.backoff().toMillis() * (1L << (attempt - 1)));
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
