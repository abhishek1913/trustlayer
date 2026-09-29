package com.trustlayer.user.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
        @NotBlank @Size(max = 200) String token,
        @NotBlank @Size(max = 200) String newPassword) {
}
