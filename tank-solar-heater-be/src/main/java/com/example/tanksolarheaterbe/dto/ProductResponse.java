package com.example.tanksolarheaterbe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {

    private Integer id;

    private Integer categoryId;

    private String categoryName;

    private Integer brandId;

    private String brandName;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal discount;

    private String imageUrl;
}
