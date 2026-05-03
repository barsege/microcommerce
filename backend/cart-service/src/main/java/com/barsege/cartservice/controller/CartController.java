package com.barsege.cartservice.controller;

import com.barsege.cartservice.dto.request.AddCartItemRequest;
import com.barsege.cartservice.dto.request.UpdateCartItemQuantityRequest;
import com.barsege.cartservice.dto.response.CartResponse;
import com.barsege.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Add item to authenticated user's cart", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(jwt.getSubject(), request);
    }

    @Operation(summary = "Get authenticated user's cart", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getCart(jwt.getSubject());
    }

    @Operation(summary = "Update item quantity in authenticated user's cart", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/items/{productId}")
    public CartResponse updateItemQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        return cartService.updateItemQuantity(jwt.getSubject(), productId, request.quantity());
    }

    @Operation(summary = "Remove item from authenticated user's cart", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("productId") Long productId
    ) {
        return cartService.removeItem(jwt.getSubject(), productId);
    }

    @Operation(summary = "Clear authenticated user's cart", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping
    public void clearCart(@AuthenticationPrincipal Jwt jwt) {
        cartService.clearCart(jwt.getSubject());
    }
}
