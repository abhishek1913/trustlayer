package com.trustlayer.user.application;

import com.trustlayer.user.api.UserDirectory;
import com.trustlayer.user.api.UserSummary;
import com.trustlayer.user.domain.User;
import com.trustlayer.user.domain.UserProfile;
import com.trustlayer.user.infrastructure.UserProfileRepository;
import com.trustlayer.user.infrastructure.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserDirectoryService implements UserDirectory {

    private final UserRepository users;
    private final UserProfileRepository profiles;

    public UserDirectoryService(UserRepository users, UserProfileRepository profiles) {
        this.users = users;
        this.profiles = profiles;
    }

    @Override
    public Optional<UserSummary> findById(UUID id) {
        return users.findById(id).flatMap(user ->
                profiles.findByUserId(id).map(profile -> ProfileService.toSummary(user, profile)));
    }

    @Override
    public Page<UserSummary> search(String emailContains, Boolean emailVerified, Pageable pageable) {
        Specification<User> spec = Specification.where(null);
        if (emailContains != null && !emailContains.isBlank()) {
            String pattern = "%" + emailContains.trim().toLowerCase().replace("%", "").replace("_", "") + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern));
        }
        if (emailVerified != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("emailVerified"), emailVerified));
        }
        Page<User> page = users.findAll(spec, pageable);
        Map<UUID, UserProfile> byUser = profiles.findByUserIdIn(page.map(User::getId).getContent()).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        return page.map(user -> ProfileService.toSummary(user, byUser.get(user.getId())));
    }
}
