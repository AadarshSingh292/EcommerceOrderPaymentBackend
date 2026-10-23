package com.example.ecommerce.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(Long cartId) {
        super("Cart is empty with id: " + cartId);
    }
}