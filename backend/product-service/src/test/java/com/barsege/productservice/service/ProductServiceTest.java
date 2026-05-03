package com.barsege.productservice.service;

import com.barsege.productservice.dto.request.CreateProductRequest;
import com.barsege.productservice.dto.response.ProductResponse;
import com.barsege.productservice.entity.Product;
import com.barsege.productservice.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldListActiveProductsWithPagination() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(productRepository.findByActiveTrue(pageable))
                .thenReturn(new PageImpl<>(List.of(product()), pageable, 1));

        Page<ProductResponse> result = productService.getProducts(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Keyboard");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product()));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.availableStock()).isEqualTo(8);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        ProductResponse response = productService.createProduct(new CreateProductRequest(
                "Mouse",
                "Wireless mouse",
                BigDecimal.valueOf(25),
                5,
                "Accessories",
                "image"
        ));

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.availableStock()).isEqualTo(5);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldReserveStock() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.reserveStock(1L, 3);

        assertThat(product.getReservedStock()).isEqualTo(5);
        verify(productRepository).save(product);
    }

    @Test
    void shouldThrowWhenInsufficientStock() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.reserveStock(1L, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldReleaseStock() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.releaseStock(1L, 1);

        assertThat(product.getReservedStock()).isEqualTo(1);
        verify(productRepository).save(product);
    }

    @Test
    void shouldConfirmStock() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.confirmStock(1L, 2);

        assertThat(product.getStock()).isEqualTo(8);
        assertThat(product.getReservedStock()).isZero();
        verify(productRepository).save(product);
    }

    private Product product() {
        return Product.builder()
                .id(1L)
                .name("Keyboard")
                .description("Mechanical")
                .price(BigDecimal.valueOf(100))
                .stock(10)
                .reservedStock(2)
                .category("Accessories")
                .imageUrl("image")
                .active(true)
                .build();
    }
}
