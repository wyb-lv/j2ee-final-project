package com.example.tanksolarheaterbe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;

    private Integer employeeId;

    /** Delivery address for this order. */
    private String address;
}
