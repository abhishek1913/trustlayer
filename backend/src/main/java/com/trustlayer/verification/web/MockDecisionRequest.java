package com.trustlayer.verification.web;

import com.trustlayer.verification.domain.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MockDecisionRequest(@NotNull VerificationStatus decision, @Size(max = 200) String reason) {
}
