package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record OrderResponse(
        Integer id,
        Integer customerId,
        String customerName,
        LocalDate date,
        String status,
        String address,
        Integer employeeId,
        BigDecimal total,
        List<OrderItemResponse> items
) {
}
