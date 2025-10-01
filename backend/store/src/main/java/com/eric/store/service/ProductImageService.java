package com.eric.store.service;

import com.eric.store.entity.ProductImage;
import com.eric.store.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageService {
    private final ProductImageRepository productImageRepository;

    public List<String> getImageUrlsByProductIdAsc(UUID productId) {
        List<ProductImage> images = productImageRepository.findAllByProductIdOrderBySortOrderAsc(productId);
        return images.stream().map(ProductImage::getImageUrl).toList();
    }
}
