package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CartLineResponse(
        Integer productId,
        String name,
        String imageUrl,
        BigDecimal price,
        BigDecimal discount,
        BigDecimal finalPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}
