package com.example.tanksolarheaterbe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartLineResponse {

    private Integer productId;

    private String name;

    private String imageUrl;

    /** Unit price before discount. */
    private BigDecimal price;

    /** Discount percentage applied to this product. */
    private BigDecimal discount;

    /** Unit price after discount. */
    private BigDecimal finalPrice;

    private Integer quantity;

    /** finalPrice * quantity. */
    private BigDecimal lineTotal;
}
