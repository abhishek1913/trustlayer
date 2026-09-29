package com.trustlayer.verification.infrastructure;

import com.trustlayer.verification.domain.VerificationSession;
import com.trustlayer.verification.domain.VerificationStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationSessionRepository extends JpaRepository<VerificationSession, UUID> {

    Optional<VerificationSession> findByProviderAndProviderRef(String provider, String providerRef);

    List<VerificationSession> findByUserIdAndStatusIn(UUID userId, Collection<VerificationStatus> statuses);

    boolean existsByUserIdAndStatus(UUID userId, VerificationStatus status);

    long countByUserId(UUID userId);

    Optional<VerificationSession> findFirstByUserIdOrderByCreatedAtDesc(UUID userId);
}
