package com.example.tanksolarheaterbe.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotNull
    private Integer productId;

    @NotNull
    @Positive
    private Integer quantity;
}
