package com.eric.store.products.controller;

import com.eric.store.products.dto.ProductCard;
import com.eric.store.products.dto.ProductCreateRequest;
import com.eric.store.products.dto.ProductQuery;
import com.eric.store.products.dto.ProductResponse;
import com.eric.store.products.entity.Product;
import com.eric.store.products.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/page")
    public ResponseEntity<Page<ProductCard>> getPageProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "true") boolean descending,

            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoryId
    ) {
        var productQuery = new ProductQuery(query, categoryId, page, size, sort, descending);
        return ResponseEntity.ok(productService.search(productQuery));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UUID> addProduct(@Valid @RequestBody ProductCreateRequest req) {
        Product product = productService.create(req);
        UUID id = product.getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        ProductResponse product = productService.getProductResponseById(id);
        return ResponseEntity.ok(product);
    }
}
