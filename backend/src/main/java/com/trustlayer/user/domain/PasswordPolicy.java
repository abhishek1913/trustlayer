package com.trustlayer.user.domain;

import com.trustlayer.shared.error.ApiException;
import com.trustlayer.shared.error.ErrorCode;
import java.nio.charset.StandardCharsets;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 10;
    private static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void enforce(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw weak("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw weak("Password must be at most " + MAX_BYTES + " bytes");
        }
        boolean upper = password.chars().anyMatch(Character::isUpperCase);
        boolean lower = password.chars().anyMatch(Character::isLowerCase);
        boolean digit = password.chars().anyMatch(Character::isDigit);
        if (!(upper && lower && digit)) {
            throw weak("Password must contain upper case, lower case and a digit");
        }
    }

    private static ApiException weak(String message) {
        return new ApiException(ErrorCode.WEAK_PASSWORD, message);
    }
}
