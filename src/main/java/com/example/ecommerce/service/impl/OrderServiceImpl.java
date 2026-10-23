package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.dto.OrderStatusUpdateRequest;
import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.PaymentStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.EmptyCartException;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.exception.OrderNotFoundException;
import com.example.ecommerce.exception.UserNotFoundException;
import com.example.ecommerce.mapper.OrderMapper;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            CartRepository cartRepository,
            ProductRepository productRepository,
            PaymentRepository paymentRepository
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrderFromCart(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EmptyCartException(userId));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException(cart.getId());
        }

        cart.getItems().forEach(this::checkStock);

        Order order = new Order();
        order.setUser(user);
        order.setOrderStatus(OrderStatus.CREATED);
        order.setOrderDate(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            reduceStock(product, cartItem.getQuantity());

            OrderItem orderItem = new OrderItem(
                    product,
                    cartItem.getQuantity(),
                    product.getPrice()
            );

            order.addOrderItem(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);

        Payment payment = buildPayment(order, request);

        if (Boolean.TRUE.equals(request.simulatePaymentFailure())) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setPaymentDate(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.CANCELLED);
            restoreStock(order);
        } else {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
            order.setOrderStatus(OrderStatus.CONFIRMED);
            cart.clearItems();
        }

        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);
        paymentRepository.save(payment);
        cartRepository.save(cart);

        return OrderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return OrderMapper.toResponse(findOrder(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = findOrder(orderId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be cancelled");
        }

        restoreStock(order);

        order.setOrderStatus(OrderStatus.CANCELLED);

        if (order.getPayment() != null
                && order.getPayment().getPaymentStatus() == PaymentStatus.PENDING) {
            order.getPayment().setPaymentStatus(PaymentStatus.FAILED);
        }

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = findOrder(orderId);

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled order status cannot be changed");
        }

        if (request.orderStatus() == OrderStatus.CANCELLED) {
            return cancelOrder(orderId);
        }

        order.setOrderStatus(request.orderStatus());

        return OrderMapper.toResponse(orderRepository.save(order));
    }

    private Payment buildPayment(Order order, OrderRequest request) {
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setTransactionReference("TXN-" + UUID.randomUUID());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        return payment;
    }

    private void checkStock(CartItem cartItem) {
        Product product = cartItem.getProduct();

        if (product.getStockQuantity() < cartItem.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName()
            );
        }
    }

    private void reduceStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }
}
