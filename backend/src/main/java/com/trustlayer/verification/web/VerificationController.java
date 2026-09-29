package com.trustlayer.verification.web;

import com.trustlayer.shared.security.AuthenticatedUser;
import com.trustlayer.verification.application.VerificationService;
import com.trustlayer.verification.application.VerificationView;
import com.trustlayer.verification.domain.IdentityVerificationProvider;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/verifications")
class VerificationController {

    private final VerificationService verification;
    private final IdentityVerificationProvider provider;

    VerificationController(VerificationService verification, IdentityVerificationProvider provider) {
        this.verification = verification;
        this.provider = provider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    VerificationView start(@AuthenticationPrincipal AuthenticatedUser principal) {
        return verification.start(principal.userId());
    }

    @GetMapping("/{id}")
    VerificationView get(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        return verification.get(principal, id);
    }

    @PostMapping("/webhook")
    void webhook(@RequestBody byte[] body, HttpServletRequest request) {
        String signature = request.getHeader(provider.signatureHeaderName());
        verification.handleWebhook(new String(body, StandardCharsets.UTF_8), signature);
    }
}
