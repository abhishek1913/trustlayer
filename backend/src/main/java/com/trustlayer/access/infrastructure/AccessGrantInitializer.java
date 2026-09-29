package com.trustlayer.access.infrastructure;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccessGrantInitializer {

    private final JdbcTemplate jdbc;

    AccessGrantInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void ensureExists(UUID userId) {
        Timestamp now = Timestamp.from(Instant.now());
        jdbc.update("""
                insert into access_grants (id, user_id, state, created_at, updated_at)
                values (?, ?, 'BLOCKED', ?, ?)
                on conflict (user_id) do nothing
                """, UUID.randomUUID(), userId, now, now);
    }
}
