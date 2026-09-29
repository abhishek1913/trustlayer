package com.trustlayer.payment.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IdempotencyStore {

    public record Claim(String requestHash, UUID transactionId) {
    }

    private final JdbcTemplate jdbc;

    IdempotencyStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean claim(UUID userId, String key, String requestHash) {
        return jdbc.update("""
                insert into idempotency_keys (id, user_id, idempotency_key, request_hash, created_at)
                values (?, ?, ?, ?, ?)
                on conflict (user_id, idempotency_key) do nothing
                """, UUID.randomUUID(), userId, key, requestHash, Timestamp.from(Instant.now())) == 1;
    }

    public Optional<Claim> find(UUID userId, String key) {
        List<Claim> rows = jdbc.query(
                "select request_hash, transaction_id from idempotency_keys where user_id = ? and idempotency_key = ?",
                (rs, i) -> new Claim(rs.getString("request_hash"), rs.getObject("transaction_id", UUID.class)),
                userId, key);
        return rows.stream().findFirst();
    }

    public void attach(UUID userId, String key, UUID transactionId) {
        jdbc.update("update idempotency_keys set transaction_id = ? where user_id = ? and idempotency_key = ?",
                transactionId, userId, key);
    }

    public void release(UUID userId, String key) {
        jdbc.update("delete from idempotency_keys where user_id = ? and idempotency_key = ? and transaction_id is null",
                userId, key);
    }
}
