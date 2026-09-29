package com.trustlayer.user.application;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.shared.event.DomainEvents;
import com.trustlayer.shared.event.EventType;
import com.trustlayer.user.domain.PasswordPolicy;
import com.trustlayer.user.domain.PasswordResetToken;
import com.trustlayer.user.domain.User;
import com.trustlayer.user.infrastructure.PasswordResetTokenRepository;
import com.trustlayer.user.infrastructure.RefreshTokenRepository;
import com.trustlayer.user.infrastructure.UserRepository;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

    private final UserRepository users;
    private final PasswordResetTokenRepository resetTokens;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties properties;
    private final DomainEvents events;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository resetTokens,
                                RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder,
                                AuthProperties properties, DomainEvents events) {
        this.users = users;
        this.resetTokens = resetTokens;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.events = events;
    }

    @Transactional
    public void request(String email) {
        Instant now = Instant.now();
        users.findByEmail(Emails.normalize(email)).ifPresent(user -> {
            resetTokens.invalidateUnused(user.getId(), now);
            String token = OpaqueTokens.generate();
            resetTokens.save(new PasswordResetToken(
                    user.getId(), OpaqueTokens.hash(token), now.plus(properties.passwordResetTtl())));
            events.publish(EventType.PASSWORD_RESET_REQUESTED, user.getId(),
                    Map.of("userId", user.getId().toString(), "resetToken", token));
        });
    }

    @Transactional
    public void confirm(String token, String newPassword) {
        Instant now = Instant.now();
        PasswordResetToken stored = resetTokens.findByTokenHash(OpaqueTokens.hash(token))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Token is invalid or expired"));
        PasswordPolicy.enforce(newPassword);
        User user = users.findById(stored.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN, "Token is invalid or expired"));
        user.changePassword(passwordEncoder.encode(newPassword));
        stored.markUsed(now);
        refreshTokens.revokeAllForUser(user.getId(), now);
    }
}
