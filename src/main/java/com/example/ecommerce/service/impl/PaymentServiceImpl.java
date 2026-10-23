package com.example.ecommerce.service.impl;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.PaymentStatus;
import com.example.ecommerce.exception.BadRequestException;
import com.example.ecommerce.exception.OrderNotFoundException;
import com.example.ecommerce.exception.PaymentNotFoundException;
import com.example.ecommerce.mapper.PaymentMapper;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.service.PaymentService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(Long orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Payment cannot be processed for a cancelled order");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseGet(() -> createPayment(order, request));

        if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
            return PaymentMapper.toResponse(payment);
        }

        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentDate(LocalDateTime.now());

        if (Boolean.TRUE.equals(request.simulatePaymentFailure())) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            order.setOrderStatus(OrderStatus.CREATED);
        } else {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }

        order.setPayment(payment);
        orderRepository.save(order);

        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return PaymentMapper.toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order id: " + orderId));

        return PaymentMapper.toResponse(payment);
    }

    private Payment createPayment(Order order, PaymentRequest request) {
        Payment payment = new Payment();

        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setTransactionReference("TXN-" + UUID.randomUUID());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        return payment;
    }
}
