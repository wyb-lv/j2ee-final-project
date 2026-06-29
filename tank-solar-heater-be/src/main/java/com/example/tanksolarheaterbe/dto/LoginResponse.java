package com.example.tanksolarheaterbe.dto;

/**
 * Returned on login. The access JWT lives in Redis and never reaches the client; instead the
 * client receives an opaque {@code sessionId} it presents as a Bearer token, plus a refresh
 * token and the profile rendered in the UI.
 */
public record LoginResponse(
        String sessionId,
        String refreshToken,
        Integer id,
        String name,
        String role
) {
}
