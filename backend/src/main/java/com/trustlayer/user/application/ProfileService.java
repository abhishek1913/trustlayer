package com.trustlayer.user.application;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import com.trustlayer.user.api.UserSummary;
import com.trustlayer.user.domain.User;
import com.trustlayer.user.domain.UserProfile;
import com.trustlayer.user.infrastructure.UserProfileRepository;
import com.trustlayer.user.infrastructure.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository users;
    private final UserProfileRepository profiles;

    public ProfileService(UserRepository users, UserProfileRepository profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public UserSummary get(UUID userId) {
        return toSummary(load(userId), loadProfile(userId));
    }

    @Transactional
    public UserSummary update(UUID userId, String fullName, String phoneNumber) {
        User user = load(userId);
        UserProfile profile = loadProfile(userId);
        profile.update(fullName.trim(), phoneNumber);
        return toSummary(user, profile);
    }

    static UserSummary toSummary(User user, UserProfile profile) {
        return new UserSummary(user.getId(), user.getEmail(), profile.getFullName(), profile.getPhoneNumber(),
                user.isEmailVerified(), user.getRole().name(), user.getCreatedAt());
    }

    private User load(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
    }

    private UserProfile loadProfile(UUID userId) {
        return profiles.findByUserId(userId).orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
    }
}
