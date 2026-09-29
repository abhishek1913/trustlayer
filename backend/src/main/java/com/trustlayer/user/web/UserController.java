package com.trustlayer.user.web;

import com.trustlayer.shared.security.AuthenticatedUser;
import com.trustlayer.user.api.UserSummary;
import com.trustlayer.user.application.ProfileService;
import com.trustlayer.user.application.RegistrationService;
import com.trustlayer.user.domain.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
class UserController {

    private final RegistrationService registration;
    private final ProfileService profiles;

    UserController(RegistrationService registration, ProfileService profiles) {
        this.registration = registration;
        this.profiles = profiles;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserSummary register(@Valid @RequestBody RegisterRequest request) {
        User user = registration.register(request.email(), request.password(), request.fullName(), request.phoneNumber());
        return profiles.get(user.getId());
    }

    @GetMapping("/me")
    UserSummary me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return profiles.get(principal.userId());
    }

    @PutMapping("/me")
    UserSummary updateMe(@AuthenticationPrincipal AuthenticatedUser principal,
                         @Valid @RequestBody UpdateProfileRequest request) {
        return profiles.update(principal.userId(), request.fullName(), request.phoneNumber());
    }
}
