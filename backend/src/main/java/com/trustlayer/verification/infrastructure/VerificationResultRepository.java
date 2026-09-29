package com.trustlayer.verification.infrastructure;

import com.trustlayer.verification.domain.VerificationResult;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationResultRepository extends JpaRepository<VerificationResult, UUID> {
}
