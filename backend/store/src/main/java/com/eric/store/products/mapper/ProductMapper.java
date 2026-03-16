package com.eric.store.products.mapper;

import com.eric.store.brands.dto.BrandResponse;
import com.eric.store.products.dto.*;
import com.eric.store.brands.entity.Brand;
import com.eric.store.products.entity.Product;
import com.eric.store.products.entity.ProductImage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    /**
     * Creates a Product.
     *
     * <p><b>Invariant:</b> A Product must always be associated with a Category
     * before it is persisted. This constructor requires a non-null Category.</p>
     */
    public Product toProduct(ProductCreateRequest req) {
        Product product = new Product();
        product.setPrice(req.price());
        product.setCurrency(req.currency());
        product.setName(req.name());
        product.setDescription(req.description());
        product.setWidth(req.width());
        product.setHeight(req.height());
        product.setDepth(req.depth());
        product.setWeight(req.weight());
        product.setStock(req.stock());
        return product;
    }

    public ProductCard toCard(Product product, String thumbnail) {
        return new ProductCard(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCurrency(),
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getStock(),
                product.getAverageRating(),
                product.getTotalRatings(),
                product.getCategory().getName(),
                thumbnail
        );
    }

    /**
     * Maps a {@link Product} entity to {@link ProductResponse}.
     *
     * IMPORTANT:
     * The given Product must be fetched with JOIN FETCH to productImages and any associated FileEntities.
     *
     * This method assumes those associations are already initialized.
     *
     * Expected fetch:
     * - ProductImages
     * - FileEntity
     */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCurrency().toString(),
                product.getWidth(),
                product.getHeight(),
                product.getDepth(),
                product.getWeight(),
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getStock(),
                product.getCategory().getId(),
                product.getAverageRating(),
                product.getTotalRatings(),
                product.getViewCount(),
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
