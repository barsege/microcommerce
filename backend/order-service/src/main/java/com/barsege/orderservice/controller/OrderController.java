package com.barsege.orderservice.controller;

import com.barsege.orderservice.dto.request.CreateOrderRequest;
import com.barsege.orderservice.dto.response.OrderResponse;
import com.barsege.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create order for authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public OrderResponse createOrder(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(jwt.getSubject(), request);
    }

    @Operation(summary = "Get authenticated user's order by id", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{orderId}")
    public OrderResponse getOrderById(@AuthenticationPrincipal Jwt jwt, @PathVariable("orderId") Long orderId) {
        return orderService.getOrderById(orderId, jwt.getSubject());
    }

    @Operation(summary = "List authenticated user's orders", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public List<OrderResponse> getOrdersByUserId(@AuthenticationPrincipal Jwt jwt) {
        return orderService.getOrdersByUserId(jwt.getSubject());
    }
}
