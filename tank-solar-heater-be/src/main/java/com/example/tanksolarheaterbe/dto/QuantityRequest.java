package com.example.tanksolarheaterbe.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Body for updating a cart line's quantity (0 removes the line). */
@Data
public class QuantityRequest {
    @NotNull
    @Min(0)
    private Integer quantity;
}
