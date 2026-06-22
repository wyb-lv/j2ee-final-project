package com.example.tanksolarheaterbe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Payload sent by the storefront when a customer checks out the cart
 * (the cart itself lives in the browser's localStorage).
 */
@Data
public class CheckoutRequest {

    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    private String customerPhone;

    private String customerAddress;

    /** COD, BANK_TRANSFER, CARD ... */
    @NotBlank
    private String paymentMethod;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
