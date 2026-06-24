package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CartResponse(
        List<CartLineResponse> lines,
        Integer itemCount,
        BigDecimal subtotal,
        BigDecimal discountTotal,
        BigDecimal total
) {
}
