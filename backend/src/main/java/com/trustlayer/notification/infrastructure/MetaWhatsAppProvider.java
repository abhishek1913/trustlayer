package com.trustlayer.notification.infrastructure;

import com.trustlayer.notification.domain.WhatsAppProvider;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "trustlayer.notification.whatsapp-provider", havingValue = "meta")
class MetaWhatsAppProvider implements WhatsAppProvider {

    private final RestClient client;
    private final String messagesPath;

    MetaWhatsAppProvider(NotificationProperties properties) {
        NotificationProperties.Meta meta = properties.meta();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.client = RestClient.builder()
                .baseUrl(meta.apiBase())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + meta.accessToken())
                .build();
        this.messagesPath = "/" + meta.apiVersion() + "/" + meta.phoneNumberId() + "/messages";
    }

    @Override
    public void send(String toPhoneNumber, String body) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", toPhoneNumber.startsWith("+") ? toPhoneNumber.substring(1) : toPhoneNumber,
                "type", "text",
                "text", Map.of("body", body));
        client.post().uri(messagesPath).contentType(MediaType.APPLICATION_JSON).body(payload).retrieve().toBodilessEntity();
    }
}
