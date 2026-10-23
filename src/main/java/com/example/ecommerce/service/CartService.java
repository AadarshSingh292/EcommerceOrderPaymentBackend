package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.dto.QuantityUpdateRequest;

public interface CartService {
    CartResponse getOrCreateCartByUserId(Long userId);
    CartResponse addProductToCart(Long cartId, CartItemRequest request);
    CartResponse updateProductQuantity(Long cartId, Long productId, QuantityUpdateRequest request);
    void removeProductFromCart(Long cartId, Long productId);
    void clearCart(Long cartId);
}