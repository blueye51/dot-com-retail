package com.eric.store.products.entity;

import com.eric.store.images.entity.FileEntity;
import com.eric.store.products.dto.ImageCreate;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "product_images",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_sort", columnNames = {"product_id", "sort_order"})
        }
)
@Getter @Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    @NonNull
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_key", nullable = false)
    @NonNull
    private FileEntity file;

    @Column(name = "sort_order", nullable = false)
    @NonNull
    private Integer sortOrder;

    public ProductImage(ImageCreate imageCreate) {
        this.product = product;
        this.file = file;
        this.sortOrder = sortOrder;
    }
}