package com.barsege.productservice.controller;

import com.barsege.productservice.dto.request.CreateProductRequest;
import com.barsege.productservice.dto.response.ProductResponse;
import com.barsege.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "List active products")
    @GetMapping
    public Page<ProductResponse> getProducts(Pageable pageable) {
        return productService.getProducts(pageable);
    }

    @Operation(summary = "Get product by id")
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable("id") Long id) {
        return productService.getProductById(id);
    }

    @Operation(summary = "Create product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }
}
