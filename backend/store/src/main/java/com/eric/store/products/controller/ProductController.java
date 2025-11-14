package com.eric.store.products.controller;

import com.eric.store.products.dto.ProductDto;
import com.eric.store.products.dto.ProductQuery;
import com.eric.store.products.service.ProductService;
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

    @GetMapping
    public ResponseEntity<Page<ProductDto>> getPageProducts(
            @RequestParam(required=false) String query,
            @RequestParam(required=false) String categoryId,
            @RequestParam(required=false) Integer page,
            @RequestParam(required=false) Integer size,
            @RequestParam(required=false) String sort,
            @RequestParam(required=false) String order
    ) {
        ProductQuery productQuery = new ProductQuery(query, categoryId, page, size, sort, order);
        Page<ProductDto> products = productService.search(productQuery);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UUID> addProduct(ProductDto productDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(productDto).getId());
    }

//    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<String> addProductImage() {
//        return ResponseEntity.ok("Not implemented yet");
//    }
}
