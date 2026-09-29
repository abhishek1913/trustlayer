package com.trustlayer.user.application;

import java.util.Locale;

final class Emails {

    private Emails() {
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
