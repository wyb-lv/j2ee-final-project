package com.example.tanksolarheaterbe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {

    private List<CartLineResponse> lines;

    /** Total number of units across all lines. */
    private Integer itemCount;

    /** Sum of unit price * quantity, before discounts. */
    private BigDecimal subtotal;

    /** Amount saved via discounts (subtotal - total). */
    private BigDecimal discountTotal;

    /** Amount payable after discounts. */
    private BigDecimal total;
}
