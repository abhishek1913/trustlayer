package com.trustlayer.user.infrastructure;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.user.application.BootstrapAdminProperties;
import com.trustlayer.user.domain.PasswordPolicy;
import com.trustlayer.user.domain.Role;
import com.trustlayer.user.domain.User;
import com.trustlayer.user.domain.UserProfile;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final BootstrapAdminProperties properties;
    private final UserRepository users;
    private final UserProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;

    AdminBootstrap(BootstrapAdminProperties properties, UserRepository users, UserProfileRepository profiles,
                   PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.configured()) {
            return;
        }
        String email = properties.email().trim().toLowerCase(Locale.ROOT);
        if (users.findByEmail(email).isPresent()) {
            return;
        }
        try {
            PasswordPolicy.enforce(properties.password());
        } catch (ApiException e) {
            log.warn("Bootstrap admin skipped: {}", e.getMessage());
            return;
        }
        User admin = users.save(new User(email, passwordEncoder.encode(properties.password()), Role.ADMIN, true));
        profiles.save(new UserProfile(admin.getId(), "Administrator", null));
        log.info("Bootstrap admin created");
    }
}
