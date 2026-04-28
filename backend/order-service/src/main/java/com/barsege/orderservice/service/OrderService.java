package com.barsege.orderservice.service;

import com.barsege.orderservice.dto.request.CreateOrderItemRequest;
import com.barsege.orderservice.dto.request.CreateOrderRequest;
import com.barsege.orderservice.dto.response.OrderResponse;
import com.barsege.orderservice.entity.Order;
import com.barsege.orderservice.entity.OrderItem;
import com.barsege.orderservice.event.OrderCreatedEvent;
import com.barsege.orderservice.event.OrderCreatedItemEvent;
import com.barsege.orderservice.mapper.OrderMapper;
import com.barsege.orderservice.messaging.OrderEventPublisher;
import com.barsege.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .userId(request.userId())
                .build();

        List<OrderItem> orderItems = request.items()
                .stream()
                .map(itemRequest -> createOrderItem(itemRequest, order))
                .toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        
        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalAmount(),
                savedOrder.getItems()
                        .stream()
                        .map(item -> new OrderCreatedItemEvent(
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity()
                        ))
                        .toList()
        );

        orderEventPublisher.publishOrderCreated(event);

        return OrderMapper.toResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        return OrderMapper.toResponse(order);
    }

    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    private OrderItem createOrderItem(CreateOrderItemRequest itemRequest, Order order) {
        BigDecimal totalPrice = itemRequest.unitPrice()
                .multiply(BigDecimal.valueOf(itemRequest.quantity()));

        return OrderItem.builder()
                .productId(itemRequest.productId())
                .productName(itemRequest.productName())
                .unitPrice(itemRequest.unitPrice())
                .quantity(itemRequest.quantity())
                .totalPrice(totalPrice)
                .order(order)
                .build();
    }
}