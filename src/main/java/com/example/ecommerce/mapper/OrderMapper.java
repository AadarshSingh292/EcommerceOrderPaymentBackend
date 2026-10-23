package com.example.ecommerce.mapper;

import com.example.ecommerce.dto.OrderItemResponse;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.entity.Order;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase(),
                        item.getSubtotal()
                ))
                .toList();

        PaymentResponse payment = order.getPayment() == null
                ? null
                : PaymentMapper.toResponse(order.getPayment());

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                items,
                order.getTotalAmount(),
                order.getOrderStatus(),
                order.getOrderDate(),
                payment
        );
    }
}