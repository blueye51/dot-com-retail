package com.eric.store.products.service;

import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.dto.*;
import com.eric.store.products.entity.Product;
import com.eric.store.products.entity.ProductImage;
import com.eric.store.products.mapper.ProductMapper;
import com.eric.store.products.repository.ProductImageRepository;
import com.eric.store.products.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;


    public Product create(ProductCreateRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category", req.categoryId()));

        Product product = new Product(
                req.price(),
                req.currency().toUpperCase(),
                req.name(),
                req.description(),
                req.width(),
                req.height(),
                req.depth(),
                req.weight(),
                req.stock()
        );

        category.addProduct(product);

        Product savedProduct = productRepository.save(product);


        if (!req.images().isEmpty()) {
            productImageService.validateSortOrders(req.images());
            for (ImageCreate imageDto : req.images()) {
                productImageService.create(imageDto, savedProduct);
            }
        }

        return savedProduct;
    }

    public Page<ProductCard> search(ProductQuery query) {
        Sort sort = Sort.by(Sort.Direction.fromString(query.order()), query.sort());
        Pageable pageable = PageRequest.of(query.page(), query.size(), sort);
        Page<Product> products;

        if (query.query().isEmpty() && query.categoryId().isEmpty()) {
            products = productRepository.findAll(pageable);
        } else if (query.categoryId().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(query.query(), pageable);
        } else if (query.query().isEmpty()) {
            products = productRepository.findByCategoryId(UUID.fromString(query.categoryId()), pageable);
        } else {
            products = productRepository.findByCategoryIdAndNameContainingIgnoreCase(
                    UUID.fromString(query.categoryId()), query.query(), pageable);
        }


        List<UUID> productIds = products.stream().map(Product::getId).toList();

        List<ProductImage> thumbnails = productImageRepository.findThumbnails(productIds);

        Map<UUID, String> urlByProductId =
                thumbnails.stream()
                        .collect(Collectors.toMap(
                                img -> img.getProduct().getId(),
                                img -> img.getFile().getUrl(),
                                (a, b) -> a // in case multiple thumbnails per productId
                        ));

        return products.map(p -> new ProductCard(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getCurrency(),
                p.getStock(),
                p.getCategory().getId(),
                p.getCreatedAt(),
                urlByProductId.get(p.getId())
        ));
    }


    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new NotFoundException("Product", id));

        return productMapper.toResponse(product);
    }
}
