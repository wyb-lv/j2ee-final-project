package com.example.tanksolarheaterbe.dto;

public record LoginResponse(
        String token,
        Integer id,
        String name,
        String role
) {
}
