package com.trustlayer.user.infrastructure;

import com.trustlayer.user.domain.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now, t.updatedAt = :now where t.familyId = :familyId and t.revokedAt is null")
    void revokeFamily(UUID familyId, Instant now);

    @Modifying
    @Query("update RefreshToken t set t.revokedAt = :now, t.updatedAt = :now where t.userId = :userId and t.revokedAt is null")
    void revokeAllForUser(UUID userId, Instant now);
}
