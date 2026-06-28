package com.example.tanksolarheaterbe.dto;

public record LoginResponse(
        String token,
        String refreshToken,
        Integer id,
        String name,
        String role
) {
}
