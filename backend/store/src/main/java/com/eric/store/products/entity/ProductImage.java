package com.eric.store.products.entity;

import com.eric.store.files.entity.FileEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
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
public class ProductImage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_key", nullable = false)
    private FileEntity file;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public ProductImage(FileEntity file, Integer sortOrder) {
        this.file = file;
        this.sortOrder = sortOrder;
    }

    /**
     * Not to be used directly. Use Product.addImage(Image) instead to
     * ensure bidirectional consistency.
     * @param product
     */
    public void setProduct(Product product) {
        this.product = Objects.requireNonNull(product);
    }
}