package com.example.ecommerce.dto;

import com.example.ecommerce.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        Boolean simulatePaymentFailure
) {
}