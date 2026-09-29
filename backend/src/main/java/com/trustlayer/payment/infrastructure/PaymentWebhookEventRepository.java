package com.trustlayer.payment.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentWebhookEventRepository {

    private final JdbcTemplate jdbc;

    PaymentWebhookEventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int insertIfAbsent(String provider, String eventId, String eventType, Instant receivedAt) {
        return jdbc.update("""
                insert into payment_webhook_events (id, provider, provider_event_id, event_type, received_at)
                values (?, ?, ?, ?, ?)
                on conflict (provider, provider_event_id) do nothing
                """, UUID.randomUUID(), provider, eventId, eventType, Timestamp.from(receivedAt));
    }
}
