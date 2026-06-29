package com.example.tanksolarheaterbe.dto;

/** A freshly-issued session id (mapped to a new JWT in Redis) paired with a rotated refresh token. */
public record TokenResponse(
        String sessionId,
        String refreshToken
) {
}
