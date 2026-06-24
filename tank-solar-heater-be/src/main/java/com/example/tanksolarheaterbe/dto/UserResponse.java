package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

@Builder
public record UserResponse(
        Integer id,
        String name,
        String email,
        String phone,
        String role,
        Boolean enabled
) {
}
