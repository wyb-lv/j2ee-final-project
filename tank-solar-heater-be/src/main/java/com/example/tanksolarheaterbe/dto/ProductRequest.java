package com.example.tanksolarheaterbe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotNull
    private Integer categoryId;

    @NotNull
    private Integer brandId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private BigDecimal price;

    private BigDecimal discount;

    @NotBlank
    private String imageUrl;
}
