package com.example.tanksolarheaterbe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Integer id;
    private String name;
    private String role;
}
