package com.trustlayer.verification.application;

import com.trustlayer.verification.api.IdentityStatusQuery;
import com.trustlayer.verification.api.VerificationSummary;
import com.trustlayer.verification.domain.VerificationStatus;
import com.trustlayer.verification.infrastructure.VerificationSessionRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class IdentityStatusService implements IdentityStatusQuery {

    private final VerificationSessionRepository sessions;

    IdentityStatusService(VerificationSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Override
    public boolean isVerified(UUID userId) {
        return sessions.existsByUserIdAndStatus(userId, VerificationStatus.VERIFIED);
    }

    @Override
    public Optional<VerificationSummary> latestFor(UUID userId) {
        long attempts = sessions.countByUserId(userId);
        return sessions.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(s -> new VerificationSummary(s.getId(), s.getStatus().name(), s.getProvider(), attempts, s.getUpdatedAt()));
    }
}
