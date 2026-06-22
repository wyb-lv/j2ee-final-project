package com.example.tanksolarheaterbe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {

    private Long id;

    private Integer orderId;

    private String paymentMethod;

    private String paymentStatus;

    private BigDecimal amount;

    private String transactionId;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;
}
