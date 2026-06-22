package com.example.tanksolarheaterbe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * The cart is held in the browser's localStorage; the client sends this
 * snapshot to the cart module to get authoritative, up-to-date pricing.
 * Nothing here is persisted to the database.
 */
@Data
public class CartRequest {

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
