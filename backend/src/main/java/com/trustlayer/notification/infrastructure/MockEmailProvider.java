package com.trustlayer.notification.infrastructure;

import com.trustlayer.notification.domain.EmailProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "trustlayer.notification.email-provider", havingValue = "mock", matchIfMissing = true)
class MockEmailProvider implements EmailProvider {

    private static final Logger log = LoggerFactory.getLogger(MockEmailProvider.class);

    private final NotificationProperties properties;

    MockEmailProvider(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void send(String to, String subject, String body) {
        if (properties.mockFail()) {
            throw new IllegalStateException("simulated email provider failure");
        }
        if (properties.mockLogBody()) {
            log.info("[MOCK EMAIL] not sent to={} subject={} body={}", Masking.mask(to), subject, body);
        } else {
            log.info("[MOCK EMAIL] not sent to={} subject={}", Masking.mask(to), subject);
        }
    }
}
