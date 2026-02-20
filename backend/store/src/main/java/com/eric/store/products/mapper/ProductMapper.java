package com.eric.store.products.mapper;

import com.eric.store.products.dto.ImageResponse;
import com.eric.store.products.dto.ProductResponse;
import com.eric.store.products.entity.Product;
import com.eric.store.products.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency(),
                product.getWidth(),
                product.getHeight(),
                product.getDepth(),
                product.getWeight(),
                product.getStock(),
                product.getCategory().getId(),
                product.getCreatedAt(),
                mapImages(product.getProductImages())
        );
    }

    private List<ImageResponse> mapImages(List<ProductImage> images) {
        return images.stream()
                .map(this::toImageResponse)
                .toList();
    }

    private ImageResponse toImageResponse(ProductImage image) {
        return new ImageResponse(
                image.getFile().getUrl(),
                image.getSortOrder()
        );
    }
}
