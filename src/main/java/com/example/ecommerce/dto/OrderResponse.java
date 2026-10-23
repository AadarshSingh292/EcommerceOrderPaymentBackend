package com.example.ecommerce.dto;

import com.example.ecommerce.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        String userName,
        List<OrderItemResponse> orderItems,
        BigDecimal totalAmount,
        OrderStatus orderStatus,
        LocalDateTime orderDate,
        PaymentResponse payment
) {
}