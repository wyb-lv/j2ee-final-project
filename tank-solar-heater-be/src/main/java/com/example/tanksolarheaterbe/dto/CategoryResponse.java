package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

@Builder
public record CategoryResponse(
        Integer id,
        String name
) {
}
