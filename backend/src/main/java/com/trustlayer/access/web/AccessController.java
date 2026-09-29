package com.trustlayer.access.web;

import com.trustlayer.access.api.AccessSummary;
import com.trustlayer.access.application.AccessService;
import com.trustlayer.shared.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/access")
class AccessController {

    private final AccessService access;

    AccessController(AccessService access) {
        this.access = access;
    }

    @GetMapping("/me")
    AccessSummary me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return access.currentFor(principal.userId());
    }

    @PostMapping("/evaluate")
    AccessSummary evaluate(@AuthenticationPrincipal AuthenticatedUser principal) {
        return access.evaluate(principal.userId());
    }
}
