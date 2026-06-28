package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductResponse(
        Integer id,
        Integer categoryId,
        String categoryName,
        Integer brandId,
        String brandName,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discount,
        BigDecimal finalPrice,
        String imageUrl
) {
}
