package com.eric.store.products.entity;

import com.eric.store.products.dto.ProductImageDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_product_images_product_sort", columnList = "product_id, sort_order"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NonNull
    @Column(nullable = false)
    private String imageUrl;

    @NonNull
    @Column(nullable = false)
    private Integer sortOrder;

    public ProductImage(ProductImageDto productImageDto) {
        this.imageUrl = productImageDto.imageUrl();
        this.sortOrder = productImageDto.sortOrder();
    }
}
