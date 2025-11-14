package com.eric.store.products.dto;

import com.eric.store.products.entity.ProductImage;

import java.util.UUID;


public record ProductImageDto(
        UUID id,
        String imageUrl,
        Integer sortOrder
) {
    public static ProductImageDto from(ProductImage p) {
        return new ProductImageDto(
                p.getId(),
                p.getImageUrl(),
                p.getSortOrder()
        );
    }
}
