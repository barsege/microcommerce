package com.barsege.cartservice.service;

import com.barsege.cartservice.dto.request.AddCartItemRequest;
import com.barsege.cartservice.dto.response.CartResponse;
import com.barsege.cartservice.entity.Cart;
import com.barsege.cartservice.entity.CartItem;
import com.barsege.cartservice.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void shouldAddItemToNewCart() {
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(1L);
            return cart;
        });

        CartResponse response = cartService.addItem("user-1", request(1));

        assertThat(response.userId()).isEqualTo("user-1");
        assertThat(response.items()).hasSize(1);
        assertThat(response.totalAmount()).isEqualByComparingTo("50");
    }

    @Test
    void shouldUpdateQuantityWhenAddingExistingItem() {
        Cart cart = cartWithItem();
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.addItem("user-1", request(2));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(3);
        assertThat(response.totalAmount()).isEqualByComparingTo("150");
    }

    @Test
    void shouldGetCartByUserId() {
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cartWithItem()));

        CartResponse response = cartService.getCart("user-1");

        assertThat(response.userId()).isEqualTo("user-1");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyCartWhenCartNotFound() {
        when(cartRepository.findByUserId("missing")).thenReturn(Optional.empty());

        CartResponse response = cartService.getCart("missing");

        assertThat(response.userId()).isEqualTo("missing");
        assertThat(response.items()).isEmpty();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldRemoveItem() {
        Cart cart = cartWithItem();
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.removeItem("user-1", 1L);

        assertThat(response.items()).isEmpty();
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldUpdateItemQuantity() {
        Cart cart = cartWithItem();
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        CartResponse response = cartService.updateItemQuantity("user-1", 1L, 4);

        assertThat(response.items().get(0).quantity()).isEqualTo(4);
        assertThat(response.totalAmount()).isEqualByComparingTo("200");
        verify(cartRepository).save(cart);
    }

    @Test
    void shouldKeepUserCartsIsolatedBySubject() {
        when(cartRepository.findByUserId("keycloak-sub-user-1")).thenReturn(Optional.of(cartWithUser("keycloak-sub-user-1")));
        when(cartRepository.findByUserId("keycloak-sub-user-2")).thenReturn(Optional.empty());

        CartResponse user1Cart = cartService.getCart("keycloak-sub-user-1");

        assertThat(user1Cart.userId()).isEqualTo("keycloak-sub-user-1");
        CartResponse user2Cart = cartService.getCart("keycloak-sub-user-2");

        assertThat(user2Cart.userId()).isEqualTo("keycloak-sub-user-2");
        assertThat(user2Cart.items()).isEmpty();
    }

    @Test
    void shouldClearCart() {
        Cart cart = cartWithItem();
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(cart));

        cartService.clearCart("user-1");

        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }

    private AddCartItemRequest request(int quantity) {
        return new AddCartItemRequest(
                null,
                1L,
                "Keyboard",
                BigDecimal.valueOf(50),
                quantity
        );
    }

    private Cart cartWithItem() {
        return cartWithUser("user-1");
    }

    private Cart cartWithUser(String userId) {
        Cart cart = Cart.builder()
                .id(1L)
                .userId(userId)
                .items(new ArrayList<>())
                .build();
        CartItem item = CartItem.builder()
                .id(1L)
                .productId(1L)
                .productName("Keyboard")
                .unitPrice(BigDecimal.valueOf(50))
                .quantity(1)
                .totalPrice(BigDecimal.valueOf(50))
                .cart(cart)
                .build();
        cart.getItems().add(item);
        return cart;
    }
}
