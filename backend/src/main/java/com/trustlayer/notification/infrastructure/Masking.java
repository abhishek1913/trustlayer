package com.trustlayer.notification.infrastructure;

final class Masking {

    private Masking() {
    }

    static String mask(String value) {
        if (value == null || value.length() < 4) {
            return "***";
        }
        int at = value.indexOf('@');
        if (at > 1) {
            return value.charAt(0) + "***" + value.substring(at);
        }
        return "***" + value.substring(value.length() - 3);
    }
}
