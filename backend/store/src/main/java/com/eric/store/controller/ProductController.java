package com.eric.store.controller;

import com.eric.store.dto.ProductDto;
import com.eric.store.dto.ProductQuery;
import com.eric.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
