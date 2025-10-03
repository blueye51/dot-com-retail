package com.eric.store.dto;

import com.eric.store.entity.ProductImage;


public record ProductImageDto(
        String imageUrl,
        Integer sortOrder
) {
    public static ProductImageDto from(ProductImage p) {
        return new ProductImageDto(
                p.getImageUrl(),
                p.getSortOrder()
        );
    }
}
