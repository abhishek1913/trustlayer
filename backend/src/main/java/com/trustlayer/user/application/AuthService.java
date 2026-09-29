package com.trustlayer.user.application;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.shared.security.JwtService;
import com.trustlayer.user.domain.RefreshToken;
import com.trustlayer.user.domain.User;
import com.trustlayer.user.infrastructure.RefreshTokenRepository;
import com.trustlayer.user.infrastructure.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthProperties properties;
    private final String unknownUserHash;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthProperties properties) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
        this.unknownUserHash = passwordEncoder.encode(OpaqueTokens.generate());
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenPair login(String email, String password) {
        Instant now = Instant.now();
        User user = users.findByEmail(Emails.normalize(email)).orElse(null);
        if (user == null) {
            passwordEncoder.matches(password, unknownUserHash);
            throw invalidCredentials();
        }
        if (user.isLocked(now)) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "Account is temporarily locked, try again later");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.registerFailedLogin(properties.maxFailedLogins(), properties.lockDuration(), now);
            throw invalidCredentials();
        }
        user.registerSuccessfulLogin();
        return issue(user, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = ApiException.class)
    public TokenPair refresh(String rawToken) {
        Instant now = Instant.now();
        RefreshToken token = refreshTokens.findByTokenHash(OpaqueTokens.hash(rawToken))
                .orElseThrow(this::invalidRefreshToken);
        if (token.isRevoked()) {
            refreshTokens.revokeFamily(token.getFamilyId(), now);
            throw invalidRefreshToken();
        }
        if (!token.isActive(now)) {
            throw invalidRefreshToken();
        }
        User user = users.findById(token.getUserId()).orElseThrow(this::invalidRefreshToken);
        token.revoke(now);
        return issue(user, token.getFamilyId());
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findByTokenHash(OpaqueTokens.hash(rawToken))
                .ifPresent(token -> refreshTokens.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    private TokenPair issue(User user, UUID familyId) {
        String refresh = OpaqueTokens.generate();
        refreshTokens.save(new RefreshToken(
                user.getId(), familyId, OpaqueTokens.hash(refresh), Instant.now().plus(properties.refreshTtl())));
        String access = jwtService.issueAccessToken(user.getId(), user.getRole().name());
        return new TokenPair(access, refresh, jwtService.accessTtlSeconds());
    }

    private ApiException invalidCredentials() {
        return new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
    }

    private ApiException invalidRefreshToken() {
        return new ApiException(ErrorCode.INVALID_REFRESH_TOKEN, "Refresh token is invalid or expired");
    }
}
