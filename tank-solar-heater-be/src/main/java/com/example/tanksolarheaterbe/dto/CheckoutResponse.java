package com.example.tanksolarheaterbe.dto;

import lombok.Builder;

@Builder
public record CheckoutResponse(
        OrderResponse order,
        PaymentResponse payment,
        /** VNPay redirect URL the storefront should send the customer to; null for COD. */
        String paymentUrl
) {
}
