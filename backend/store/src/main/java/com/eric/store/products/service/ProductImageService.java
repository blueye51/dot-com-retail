package com.eric.store.products.service;

import com.eric.store.products.dto.ImageCreate;
import com.eric.store.products.entity.ProductImage;
import com.eric.store.products.repository.ProductImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {
    private final ProductImageRepository productImageRepository;

    private List<ProductImage> getImagesByProductIdAsc(UUID productId) {
        return productImageRepository.findAllByProductIdOrderBySortOrderAsc(productId);
    }

    public List<ImageCreate> getImageDtosByProductIdAsc(UUID productId) {
        List<ProductImage> images = productImageRepository.findAllByProductIdOrderBySortOrderAsc(productId);
        return images.stream().map(ImageCreate::from).toList();
    }
}
