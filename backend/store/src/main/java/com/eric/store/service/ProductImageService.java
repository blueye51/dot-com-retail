package com.eric.store.service;

import com.eric.store.dto.ProductImageDto;
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

    private List<ProductImage> getImagesByProductIdAsc(UUID productId) {
        return productImageRepository.findAllByProductIdOrderBySortOrderAsc(productId);
    }

    public List<ProductImageDto> getImageDtosByProductIdAsc(UUID productId) {
        List<ProductImage> images = productImageRepository.findAllByProductIdOrderBySortOrderAsc(productId);
        return images.stream().map(ProductImageDto::from).toList();
    }
}
