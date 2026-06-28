package com.example.tanksolarheaterbe.dto;

/** A freshly-issued access token paired with a rotated refresh token. */
public record TokenResponse(
        String token,
        String refreshToken
) {
}
