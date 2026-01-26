package com.eric.store.products.service;

import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.categories.service.CategoryService;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.dto.ProductCreateRequest;
import com.eric.store.products.dto.ProductQuery;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;
    private final CategoryRepository categoryRepository;


    public Product create(ProductCreateRequest req) {
        Product product = new Product(
                req.price(),
                req.currency(),
                req.name(),
                req.description(),
                req.width(),
                req.height(),
                req.depth(),
                req.weight(),
                req.stock()
        );

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category", req.categoryId()));

        category.addProduct(product);

        return productRepository.save(product);
    }

    public Page<Pr> search(ProductQuery query) {
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

        return products.map(product -> ProductDto.from(product, product.getCategory().getId(), productImageService.getImageDtosByProductIdAsc(product.getId())));
    }

}
