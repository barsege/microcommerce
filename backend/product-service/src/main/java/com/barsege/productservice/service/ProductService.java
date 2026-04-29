package com.barsege.productservice.service;

import com.barsege.productservice.dto.request.CreateProductRequest;
import com.barsege.productservice.dto.response.ProductResponse;
import com.barsege.productservice.entity.Product;
import com.barsege.productservice.event.PaymentFailedEvent;
import com.barsege.productservice.mapper.ProductMapper;
import com.barsege.productservice.repository.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(ProductMapper::toResponse);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        return ProductMapper.toResponse(product);
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .reservedStock(0)
                .category(request.category())
                .imageUrl(request.imageUrl())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);

        return ProductMapper.toResponse(savedProduct);
    }
    
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int availableStock = product.getStock() - product.getReservedStock();

        if (availableStock < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + productId);
        }

        product.setReservedStock(product.getReservedStock() + quantity);
        productRepository.save(product);
    }
    
    @Transactional
    public void releaseStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int newReservedStock = product.getReservedStock() - quantity;

        if (newReservedStock < 0) {
            newReservedStock = 0;
        }

        product.setReservedStock(newReservedStock);
        productRepository.save(product);
    }
}
