package com.example.tanksolarheaterbe.dto;

import com.example.tanksolarheaterbe.entities.PaymentMethod;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        PaymentMethod paymentMethod,
        String paymentStatus,
        LocalDateTime paidAt,
        List<OrderItemResponse> items
) {
}
