package com.trustlayer.user.application;

public record TokenPair(String accessToken, String refreshToken, long expiresInSeconds) {
}
