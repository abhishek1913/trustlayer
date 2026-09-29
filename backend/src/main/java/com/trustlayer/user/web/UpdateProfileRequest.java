package com.trustlayer.user.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 200) String fullName,
        @Pattern(regexp = "^\\+[1-9]\\d{7,14}$", message = "must be in E.164 format, for example +919876543210") String phoneNumber) {
}
