package com.eric.store.products.service;

import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.common.util.StringUtils;
import com.eric.store.common.util.UuidUtils;
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

        Product product = productMapper.toProduct(req);

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

        String q = StringUtils.normalize(query.query());
        UUID categoryId = UuidUtils.parseUuidOrNull(query.categoryId());

        Page<Product> products = findProducts(q, categoryId, pageable);

        Map<UUID, String> thumbnailUrlByProductId = loadThumbnailUrls(products);

        return products.map(p -> productMapper.toCard(p, thumbnailUrlByProductId.get(p.getId())));
    }

    private Page<Product> findProducts(String q, UUID categoryId, Pageable pageable) {
        return productRepository.search(q, categoryId, pageable);
    }

    private Map<UUID, String> loadThumbnailUrls(Page<Product> products) {
        List<UUID> productIds = products.stream().map(Product::getId).toList();
        if (productIds.isEmpty()) return Map.of();

        return productImageRepository.findThumbnails(productIds).stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        img -> img.getFile().getUrl(),
                        (a, b) -> a
                ));
    }


    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new NotFoundException("Product", id));

        return productMapper.toResponse(product);
    }
}
