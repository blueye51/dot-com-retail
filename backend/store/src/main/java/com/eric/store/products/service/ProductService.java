package com.eric.store.products.service;

import com.eric.store.brands.service.BrandService;
import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.dto.*;
import com.eric.store.brands.entity.Brand;
import com.eric.store.products.entity.Product;
import com.eric.store.products.mapper.ProductMapper;
import com.eric.store.products.repository.ProductRepository;
import com.eric.store.products.repository.ProductSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageService productImageService;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final BrandService brandService;

    @Transactional
    public Product create(ProductCreateRequest req) {
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new NotFoundException("Category", req.categoryId()));

        Product product = productMapper.toProduct(req);

        if (req.brandId() != null) {
            Brand brand = brandService.findById(req.brandId());
            product.setBrand(brand);
        }

        product.setCategory(category);
        Product savedProduct = productRepository.save(product);


        if (!req.images().isEmpty()) {
            productImageService.validateSortOrders(req.images());
            for (ImageCreate imageDto : req.images()) {
                productImageService.create(imageDto, savedProduct);
            }
        }

        return savedProduct;
    }

    @Transactional(readOnly = true)
    public Page<ProductCard> search(ProductQuery query) {
        Pageable pageable = toPageable(query);
        Page<Product> products = findProducts(query, pageable);
        Map<UUID, String> thumbnailUrlByProductId = productImageService.loadThumbnailUrls(products);
        return products.map(p -> productMapper.toCard(p, thumbnailUrlByProductId.get(p.getId())));
    }

    private Pageable toPageable(ProductQuery q) {
        Sort.Direction direction = q.descending() ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, q.sort().getValue());
        return PageRequest.of(q.page(), q.size(), sort);
    }

    private Page<Product> findProducts(ProductQuery q, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.fetchBrand()
                .and(ProductSpecification.fetchCategory())
                .and(ProductSpecification.hasCategory(q.categoryId()))
                .and(ProductSpecification.hasBrand(q.brandId()))
                .and(ProductSpecification.nameContains(q.query()))
                .and(ProductSpecification.priceGreaterThanOrEqual(q.minPrice()))
                .and(ProductSpecification.priceLessThanOrEqual(q.maxPrice()));
        return productRepository.findAll(spec, pageable);
    }


    @Transactional
    public ProductResponse getProductResponseById(UUID id) {
        productRepository.incrementViewCount(id);
        return productMapper.toResponse(findProductWithImagesById(id));
    }

    private Product findProductWithImagesById(UUID id) {
        return productRepository.findByIdWithImages(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }
}
