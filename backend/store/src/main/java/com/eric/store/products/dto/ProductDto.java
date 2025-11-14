package com.eric.store.products.dto;

import com.eric.store.products.entity.Product;

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
        UUID categoryId,
        List<ProductImageDto> images
) {
    public static ProductDto from(Product p,UUID categoryId, List<ProductImageDto> images) {
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
                categoryId,
                images
        );
    }
}
