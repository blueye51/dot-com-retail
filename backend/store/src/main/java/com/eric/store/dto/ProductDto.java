package com.eric.store.dto;

import com.eric.store.entity.Product;
import com.eric.store.entity.ProductImage;

import java.util.List;
import java.util.UUID;

public record ProductDto(
        UUID id,
        String name,
        String description,
        String price,
        String currency,
        String width,
        String height,
        String depth,
        String weight,
        Integer stock,
        List<ProductImageDto> images
) {
    public static ProductDto from(Product p, List<ProductImageDto> images) {
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getPrice().toPlainString(),
                p.getCurrency(),
                p.getWidth().toPlainString(),
                p.getHeight().toPlainString(),
                p.getDepth().toPlainString(),
                p.getWeight().toPlainString(),
                p.getStock(),
                images
        );
    }
}
