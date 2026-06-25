package com.example.tanksolarheaterbe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequest {

    @NotBlank
    private String name;
}
