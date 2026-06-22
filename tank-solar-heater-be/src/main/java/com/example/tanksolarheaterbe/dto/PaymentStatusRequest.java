package com.example.tanksolarheaterbe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Used by staff to mark a payment as PAID / FAILED / REFUNDED. */
@Data
public class PaymentStatusRequest {

    @NotBlank
    private String status;

    private String transactionId;
}
