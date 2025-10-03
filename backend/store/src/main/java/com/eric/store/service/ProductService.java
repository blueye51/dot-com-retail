package com.eric.store.service;

import com.eric.store.dto.ProductDto;
import com.eric.store.dto.ProductQuery;
import com.eric.store.entity.Product;
import com.eric.store.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;

    public Product fromDto(ProductDto productDto) {
        return new Product(productDto);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Page<ProductDto> search(ProductQuery query) {
        Sort sort = Sort.by(Sort.Direction.fromString(query.order()), query.sort());
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);
        Page<Product> products;

        if (query.query().isEmpty() && query.categoryId().isEmpty()) {
            products = productRepository.findAll(pageable);
        } else if (query.query().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(query.query(), pageable);
        } else if (query.categoryId().isEmpty()) {
            products = productRepository.findByCategoryId(UUID.fromString(query.categoryId()), pageable);
        } else {
            products = productRepository.findByCategoryIdAndNameContainingIgnoreCase(UUID.fromString(query.categoryId()), query.query(), pageable);
        }

        return products.map(product -> ProductDto.from(product, productImageService.getImageDtosByProductIdAsc(product.getId())));
    }

}
