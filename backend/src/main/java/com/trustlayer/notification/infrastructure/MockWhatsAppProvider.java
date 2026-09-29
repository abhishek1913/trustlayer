package com.trustlayer.notification.infrastructure;

import com.trustlayer.notification.domain.WhatsAppProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "trustlayer.notification.whatsapp-provider", havingValue = "mock", matchIfMissing = true)
class MockWhatsAppProvider implements WhatsAppProvider {

    private static final Logger log = LoggerFactory.getLogger(MockWhatsAppProvider.class);

    private final NotificationProperties properties;

    MockWhatsAppProvider(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void send(String toPhoneNumber, String body) {
        if (properties.mockFail()) {
            throw new IllegalStateException("simulated whatsapp provider failure");
        }
        if (properties.mockLogBody()) {
            log.info("[MOCK WHATSAPP] not sent to={} body={}", Masking.mask(toPhoneNumber), body);
        } else {
            log.info("[MOCK WHATSAPP] not sent to={}", Masking.mask(toPhoneNumber));
        }
    }
}
