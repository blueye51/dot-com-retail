package com.eric.store.service;

import com.eric.store.dto.ProductDto;
import com.eric.store.dto.ProductQuery;
import com.eric.store.entity.Product;
import com.eric.store.repository.CategoryRepository;
import com.eric.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;


    public Page<ProductDto> search(ProductQuery query) {
        Sort sort = Sort.by(Sort.Direction.fromString(query.order()), query.sort());
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);
        Page<Product> products;

        if (query.query().isEmpty() && query.category().isEmpty()) {
            products = productRepository.findAll(pageable);
        } else if (query.query().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(query.query(), pageable);
        } else if (query.category().isEmpty()) {
            products = productRepository.findByCategoryId(categoryService.getProductByName(query.category()).getId(), pageable);
        } else {
            products = productRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryService.getProductByName(query.category()).getId(), query.query(), pageable);
        }

        return products.map(product -> ProductDto.from(product, productImageService.getImageUrlsByProductIdAsc(product.getId())));
    }

}
