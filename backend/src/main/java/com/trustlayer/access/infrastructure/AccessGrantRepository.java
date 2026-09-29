package com.trustlayer.access.infrastructure;

import com.trustlayer.access.domain.AccessGrant;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AccessGrantRepository extends JpaRepository<AccessGrant, UUID> {

    Optional<AccessGrant> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from AccessGrant g where g.userId = :userId")
    Optional<AccessGrant> lockByUserId(UUID userId);
}
