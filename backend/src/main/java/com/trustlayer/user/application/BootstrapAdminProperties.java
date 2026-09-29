package com.trustlayer.user.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trustlayer.bootstrap-admin")
public record BootstrapAdminProperties(String email, String password) {

    public boolean configured() {
        return email != null && !email.isBlank() && password != null && !password.isBlank();
    }
}
