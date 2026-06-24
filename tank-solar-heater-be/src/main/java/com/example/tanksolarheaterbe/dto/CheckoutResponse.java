package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

@Builder
public record CheckoutResponse(
        OrderResponse order,
        PaymentResponse payment
) {
}
