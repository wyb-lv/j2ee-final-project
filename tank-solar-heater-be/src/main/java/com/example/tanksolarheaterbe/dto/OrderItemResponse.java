package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OrderItemResponse(
        Integer productId,
        String productName,
        Integer quantity,
        BigDecimal price,
        BigDecimal discount
) {
}
