package com.trustlayer.verification.application;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.shared.event.DomainEvents;
import com.trustlayer.shared.event.EventType;
import com.trustlayer.shared.security.AuthenticatedUser;
import com.trustlayer.user.api.UserDirectory;
import com.trustlayer.verification.domain.IdentityVerificationProvider;
import com.trustlayer.verification.domain.ProviderOutcome;
import com.trustlayer.verification.domain.ProviderSession;
import com.trustlayer.verification.domain.ProviderWebhookEvent;
import com.trustlayer.verification.domain.VerificationResult;
import com.trustlayer.verification.domain.VerificationSession;
import com.trustlayer.verification.domain.VerificationStatus;
import com.trustlayer.verification.infrastructure.VerificationProperties;
import com.trustlayer.verification.infrastructure.VerificationResultRepository;
import com.trustlayer.verification.infrastructure.VerificationSessionRepository;
import com.trustlayer.verification.infrastructure.VerificationWebhookEventRepository;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VerificationService {

    private static final Logger log = LoggerFactory.getLogger(VerificationService.class);
    private static final EnumSet<VerificationStatus> ACTIVE = EnumSet.of(VerificationStatus.PENDING, VerificationStatus.IN_PROGRESS);

    private final VerificationSessionRepository sessions;
    private final VerificationResultRepository results;
    private final VerificationWebhookEventRepository webhookEvents;
    private final IdentityVerificationProvider provider;
    private final VerificationProperties properties;
    private final UserDirectory users;
    private final DomainEvents events;

    public VerificationService(VerificationSessionRepository sessions, VerificationResultRepository results,
                               VerificationWebhookEventRepository webhookEvents, IdentityVerificationProvider provider,
                               VerificationProperties properties, UserDirectory users, DomainEvents events) {
        this.sessions = sessions;
        this.results = results;
        this.webhookEvents = webhookEvents;
        this.provider = provider;
        this.properties = properties;
        this.users = users;
        this.events = events;
        if (provider.isMock()) {
            log.warn("Identity verification runs on the MOCK provider: no real document or selfie check is performed");
        }
    }

    @Transactional
    public VerificationView start(UUID userId) {
        boolean emailVerified = users.findById(userId).map(u -> u.emailVerified()).orElse(false);
        if (!emailVerified) {
            throw new ApiException(ErrorCode.EMAIL_NOT_VERIFIED, "Verify your email before starting identity verification");
        }
        Instant now = Instant.now();
        boolean activeExists = false;
        for (VerificationSession stale : sessions.findByUserIdAndStatusIn(userId, ACTIVE)) {
            if (stale.isExpiredAt(now)) {
                stale.markExpired();
            } else {
                activeExists = true;
            }
        }
        sessions.flush();
        if (sessions.existsByUserIdAndStatus(userId, VerificationStatus.VERIFIED)) {
            throw new ApiException(ErrorCode.IDENTITY_ALREADY_VERIFIED, "Identity is already verified");
        }
        if (activeExists) {
            throw new ApiException(ErrorCode.VERIFICATION_IN_PROGRESS, "A verification session is already in progress");
        }
        if (sessions.countByUserId(userId) >= properties.maxAttempts()) {
            throw new ApiException(ErrorCode.VERIFICATION_ATTEMPTS_EXHAUSTED, "Maximum verification attempts reached");
        }
        ProviderSession providerSession = provider.createSession(userId);
        VerificationSession session = new VerificationSession(
                userId, provider.name(), providerSession.providerRef(), now.plus(properties.sessionTtl()));
        try {
            sessions.saveAndFlush(session);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.VERIFICATION_IN_PROGRESS, "A verification session is already in progress");
        }
        return VerificationView.of(session, provider.isMock(), providerSession.redirectUrl());
    }

    @Transactional
    public VerificationView get(AuthenticatedUser caller, UUID sessionId) {
        VerificationSession session = sessions.findById(sessionId)
                .filter(s -> caller.isAdmin() || s.getUserId().equals(caller.userId()))
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Verification session not found"));
        refresh(session);
        return VerificationView.of(session, isMock(session), null);
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        ProviderWebhookEvent event = provider.parseWebhook(payload, signature);
        int inserted = webhookEvents.insertIfAbsent(
                UUID.randomUUID(), provider.name(), event.eventId(), event.eventType(), Instant.now());
        if (inserted == 0) {
            log.info("Duplicate identity webhook skipped");
            return;
        }
        if (event.providerRef() == null || event.outcome() == null) {
            return;
        }
        sessions.findByProviderAndProviderRef(provider.name(), event.providerRef())
                .ifPresentOrElse(
                        session -> apply(session, event.outcome(), false),
                        () -> log.warn("Identity webhook for unknown session"));
    }

    @Transactional(noRollbackFor = ApiException.class)
    public VerificationView decideMock(UUID sessionId, VerificationStatus decision, String reason) {
        if (!provider.isMock()) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Not available");
        }
        VerificationSession session = sessions.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Verification session not found"));
        apply(session, new ProviderOutcome(decision, reason), true);
        return VerificationView.of(session, true, null);
    }

    private void refresh(VerificationSession session) {
        if (session.getStatus().isTerminal()) {
            return;
        }
        if (session.isExpiredAt(Instant.now())) {
            session.markExpired();
            return;
        }
        if (!session.getProvider().equals(provider.name())) {
            return;
        }
        try {
            provider.getResult(session.getProviderRef()).ifPresent(outcome -> apply(session, outcome, false));
        } catch (ApiException e) {
            log.warn("Could not refresh verification session from provider");
        }
    }

    private void apply(VerificationSession session, ProviderOutcome outcome, boolean strict) {
        Instant now = Instant.now();
        if (session.getStatus().isTerminal()) {
            log.info("Ignoring outcome for finished verification session");
            return;
        }
        if (session.isExpiredAt(now)) {
            session.markExpired();
            if (strict) {
                throw new ApiException(ErrorCode.VERIFICATION_SESSION_EXPIRED, "Verification session has expired");
            }
            return;
        }
        switch (outcome.status()) {
            case IN_PROGRESS -> session.markInProgress();
            case EXPIRED -> session.markExpired();
            case VERIFIED, REJECTED -> finish(session, outcome, now);
            default -> {
            }
        }
    }

    private void finish(VerificationSession session, ProviderOutcome outcome, Instant now) {
        session.complete(outcome.status());
        results.save(new VerificationResult(session.getId(), outcome.status(), outcome.reason(), now));
        EventType type = outcome.status() == VerificationStatus.VERIFIED ? EventType.IDENTITY_VERIFIED : EventType.IDENTITY_REJECTED;
        events.publish(type, session.getId(),
                Map.of("userId", session.getUserId().toString(), "sessionId", session.getId().toString()));
    }

    private boolean isMock(VerificationSession session) {
        return "mock".equals(session.getProvider());
    }
}
