package com.eric.store.products.entity;

import com.eric.store.categories.entity.Category;
import com.eric.store.products.dto.ProductDto;
import com.eric.store.orders.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @NonNull
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal price;

    @NonNull
    @Column(nullable = false)
    private String currency;

    @NonNull
    @Column(nullable = false)
    private String name;

    @NonNull
    private String description;

    @NonNull
    private BigDecimal width;

    @NonNull
    private BigDecimal height;

    @NonNull
    private BigDecimal depth;

    @NonNull
    private BigDecimal weight;

    @NonNull
    @Column(nullable = false)
    private Integer stock;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    //Timestamps automation
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }


    public Product(ProductDto productDto) {
        this.price = new BigDecimal(productDto.price());
        this.currency = productDto.currency();
        this.name = productDto.name();
        this.description = productDto.description();
        this.width = new BigDecimal(productDto.width());
        this.height = new BigDecimal(productDto.height());
        this.depth = new BigDecimal(productDto.depth());
        this.weight = new BigDecimal(productDto.weight());
        this.stock = productDto.stock();
        productDto.images().forEach(productImageDto -> this.images.add(new ProductImage(productImageDto)));
    }
}
