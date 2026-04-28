package com.barsege.cartservice.controller;

import com.barsege.cartservice.dto.request.AddCartItemRequest;
import com.barsege.cartservice.dto.response.CartResponse;
import com.barsege.cartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(request);
    }

    @GetMapping("/{userId}")
    public CartResponse getCart(@PathVariable("userId") String userId) {
        return cartService.getCart(userId);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public CartResponse removeItem(
            @PathVariable("userId") String userId,
            @PathVariable("productId") Long productId
    ) {
        return cartService.removeItem(userId, productId);
    }

    @DeleteMapping("/{userId}")
    public void clearCart(@PathVariable("userId") String userId) {
        cartService.clearCart(userId);
    }
}