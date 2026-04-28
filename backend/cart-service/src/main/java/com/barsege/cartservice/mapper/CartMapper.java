package com.barsege.cartservice.mapper;

import com.barsege.cartservice.dto.response.CartItemResponse;
import com.barsege.cartservice.dto.response.CartResponse;
import com.barsege.cartservice.entity.Cart;
import com.barsege.cartservice.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

public class CartMapper {

    private CartMapper() {
    }

    public static CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(CartMapper::toItemResponse)
                .toList();

        BigDecimal totalAmount = cart.getItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items,
                totalAmount
        );
    }

    private static CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getTotalPrice()
        );
    }
}
