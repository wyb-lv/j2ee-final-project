package com.example.tanksolarheaterbe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {

    private OrderResponse order;

    private PaymentResponse payment;
}
