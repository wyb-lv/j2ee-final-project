package com.example.tanksolarheaterbe.dto;

import com.example.tanksolarheaterbe.entities.PaymentMethod;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PaymentResponse(
        Long id,
        Integer orderId,
        PaymentMethod paymentMethod,
        String paymentStatus,
        BigDecimal amount,
        String transactionId,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
}
