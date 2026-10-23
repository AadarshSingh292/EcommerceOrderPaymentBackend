package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.dto.QuantityUpdateRequest;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.CartNotFoundException;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.ProductNotFoundException;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.mapper.CartMapper;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CartResponse getOrCreateCartByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(new Cart(user)));

        return CartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addProductToCart(Long cartId, CartItemRequest request) {
        Cart cart = findCart(cartId);
        Product product = findProduct(request.productId());
        CartItem existingItem = findCartItem(cart, request.productId());

        int newQuantity = existingItem == null
                ? request.quantity()
                : existingItem.getQuantity() + request.quantity();

        checkStock(product, newQuantity);

        if (existingItem == null) {
            cart.addItem(new CartItem(product, request.quantity()));
        } else {
            existingItem.setQuantity(newQuantity);
        }

        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse updateProductQuantity(Long cartId, Long productId, QuantityUpdateRequest request) {
        Cart cart = findCart(cartId);
        Product product = findProduct(productId);
        CartItem item = findRequiredCartItem(cart, productId);

        checkStock(product, request.quantity());
        item.setQuantity(request.quantity());

        return CartMapper.toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public void removeProductFromCart(Long cartId, Long productId) {
        Cart cart = findCart(cartId);
        CartItem item = findRequiredCartItem(cart, productId);

        cart.removeItem(item);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = findCart(cartId);
        cart.clearItems();
        cartRepository.save(cart);
    }

    private Cart findCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException(cartId));
    }

    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private CartItem findCartItem(Cart cart, Long productId) {
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private CartItem findRequiredCartItem(Cart cart, Long productId) {
        CartItem item = findCartItem(cart, productId);

        if (item == null) {
            throw new BadRequestException("Product is not present in this cart: " + productId);
        }

        return item;
    }

    private void checkStock(Product product, int requestedQuantity) {
        if (product.getStockQuantity() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Only " + product.getStockQuantity()
                            + " item(s) available for product: " + product.getName()
            );
        }
    }
}
