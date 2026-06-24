package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentResponse(
        Long id,
        Integer orderId,
        String paymentMethod,
        String paymentStatus,
        BigDecimal amount,
        String transactionId,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
}
