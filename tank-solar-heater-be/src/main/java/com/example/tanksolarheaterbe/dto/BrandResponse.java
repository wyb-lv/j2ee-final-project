package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

@Builder
public record BrandResponse(
        Integer id,
        String name
) {
}
