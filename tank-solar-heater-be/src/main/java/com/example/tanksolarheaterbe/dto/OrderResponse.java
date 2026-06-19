package com.example.tanksolarheaterbe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Integer id;

    private Integer customerId;

    private LocalDate date;

    private String status;

    private Integer employeeId;

    private BigDecimal total;

    private List<OrderItemResponse> items;
}
