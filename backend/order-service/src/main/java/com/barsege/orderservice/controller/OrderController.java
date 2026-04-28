package com.barsege.orderservice.controller;

import com.barsege.orderservice.dto.request.CreateOrderRequest;
import com.barsege.orderservice.dto.response.OrderResponse;
import com.barsege.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@PathVariable("orderId") Long orderId) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping("/users/{userId}")
    public List<OrderResponse> getOrdersByUserId(@PathVariable("userId") String userId) {
        return orderService.getOrdersByUserId(userId);
    }
}