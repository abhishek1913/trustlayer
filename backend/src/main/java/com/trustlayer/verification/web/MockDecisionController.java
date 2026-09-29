package com.trustlayer.verification.web;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.verification.application.VerificationService;
import com.trustlayer.verification.application.VerificationView;
import com.trustlayer.verification.domain.VerificationStatus;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verifications")
@ConditionalOnProperty(name = "trustlayer.verification.provider", havingValue = "mock", matchIfMissing = true)
class MockDecisionController {

    private final VerificationService verification;

    MockDecisionController(VerificationService verification) {
        this.verification = verification;
    }

    @PostMapping("/{id}/mock-decision")
    @PreAuthorize("hasRole('ADMIN')")
    VerificationView decide(@PathVariable UUID id, @Valid @RequestBody MockDecisionRequest request) {
        if (request.decision() != VerificationStatus.VERIFIED && request.decision() != VerificationStatus.REJECTED) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Decision must be VERIFIED or REJECTED");
        }
        return verification.decideMock(id, request.decision(), request.reason());
    }
}
