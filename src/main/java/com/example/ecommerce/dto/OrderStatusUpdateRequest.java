package com.example.ecommerce.dto;

import com.example.ecommerce.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(
        @NotNull(message = "Order status is required")
        OrderStatus orderStatus
) {
}