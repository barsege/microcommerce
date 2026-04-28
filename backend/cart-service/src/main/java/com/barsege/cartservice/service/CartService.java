package com.barsege.cartservice.service;

import com.barsege.cartservice.dto.request.AddCartItemRequest;
import com.barsege.cartservice.dto.response.CartResponse;
import com.barsege.cartservice.entity.Cart;
import com.barsege.cartservice.entity.CartItem;
import com.barsege.cartservice.mapper.CartMapper;
import com.barsege.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(request.userId())
                .orElseGet(() -> Cart.builder()
                        .userId(request.userId())
                        .build());

        CartItem existingItem = cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(request.productId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + request.quantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(request.productId())
                    .productName(request.productName())
                    .unitPrice(request.unitPrice())
                    .quantity(request.quantity())
                    .totalPrice(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())))
                    .cart(cart)
                    .build();

            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return CartMapper.toResponse(savedCart);
    }

    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        return CartMapper.toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(String userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        cart.getItems().removeIf(item -> item.getProductId().equals(productId));

        Cart savedCart = cartRepository.save(cart);
        return CartMapper.toResponse(savedCart);
    }

    @Transactional
    public void clearCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
