package com.trustlayer.user.application;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.shared.event.DomainEvents;
import com.trustlayer.shared.event.EventType;
import com.trustlayer.user.domain.EmailVerificationToken;
import com.trustlayer.user.domain.PasswordPolicy;
import com.trustlayer.user.domain.Role;
import com.trustlayer.user.domain.User;
import com.trustlayer.user.domain.UserProfile;
import com.trustlayer.user.infrastructure.EmailVerificationTokenRepository;
import com.trustlayer.user.infrastructure.UserProfileRepository;
import com.trustlayer.user.infrastructure.UserRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final EmailVerificationTokenRepository verificationTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;
    private final DomainEvents events;

    public RegistrationService(UserRepository users, UserProfileRepository profiles,
                               EmailVerificationTokenRepository verificationTokens, PasswordEncoder passwordEncoder,
                               AuthProperties properties, DomainEvents events) {
        this.users = users;
        this.profiles = profiles;
        this.verificationTokens = verificationTokens;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.events = events;
    }

    @Transactional
    public User register(String email, String password, String fullName, String phoneNumber) {
        String normalized = Emails.normalize(email);
        PasswordPolicy.enforce(password);
        if (users.findByEmail(normalized).isPresent()) {
            throw emailTaken();
        }
        User user;
        try {
            user = users.saveAndFlush(new User(normalized, passwordEncoder.encode(password), Role.USER, false));
        } catch (DataIntegrityViolationException e) {
            throw emailTaken();
        }
        profiles.save(new UserProfile(user.getId(), fullName.trim(), phoneNumber));
        String token = OpaqueTokens.generate();
        verificationTokens.save(new EmailVerificationToken(
                user.getId(), OpaqueTokens.hash(token), Instant.now().plus(properties.emailVerificationTtl())));
        events.publish(EventType.USER_REGISTERED, user.getId(),
                Map.of("userId", user.getId().toString(), "verificationToken", token));
        return user;
    }

    @Transactional
    public void verifyEmail(String token) {
        Instant now = Instant.now();
        EmailVerificationToken stored = verificationTokens.findByTokenHash(OpaqueTokens.hash(token))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Token is invalid or expired"));
        User user = users.findById(stored.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Token is invalid or expired"));
        stored.markUsed(now);
        if (!user.isEmailVerified()) {
            user.markEmailVerified();
            events.publish(EventType.EMAIL_VERIFIED, user.getId(), Map.of("userId", user.getId().toString()));
        }
    }

    private ApiException emailTaken() {
        return new ApiException(ErrorCode.EMAIL_ALREADY_REGISTERED, "Email is already registered");
    }
}
