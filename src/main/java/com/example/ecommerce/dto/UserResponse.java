package com.example.ecommerce.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        LocalDateTime createdAt
) {
}
