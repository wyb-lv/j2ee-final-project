package com.example.tanksolarheaterbe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Integer id;

    private String name;

    private String email;

    private String address;

    private String phone;

    private String role;

    private Boolean enabled;
}
