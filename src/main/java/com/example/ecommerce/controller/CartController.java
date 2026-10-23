package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.dto.QuantityUpdateRequest;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCartByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCartByUserId(userId));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> addProductToCart(
            @PathVariable Long cartId,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addProductToCart(cartId, request));
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<CartResponse> updateProductQuantity(
            @PathVariable Long cartId,
            @PathVariable Long productId,
            @Valid @RequestBody QuantityUpdateRequest request
    ) {
        return ResponseEntity.ok(
                cartService.updateProductQuantity(cartId, productId, request)
        );
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public ResponseEntity<Void> removeProductFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId
    ) {
        cartService.removeProductFromCart(cartId, productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }
}