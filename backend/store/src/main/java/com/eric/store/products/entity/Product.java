package com.eric.store.products.entity;

import com.eric.store.brands.entity.Brand;
import com.eric.store.categories.entity.Category;
import com.eric.store.orders.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(precision = 17, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyProvider currency;

    @Column(nullable = false)
    private String name;

    private String description;

    private BigDecimal width;

    private BigDecimal height;

    private BigDecimal depth;

    private BigDecimal weight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private double averageRating = 0.0;

    @Column(nullable = false)
    private int totalRatings = 0;

    @Column(nullable = false)
    private long viewCount = 0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.PERSIST)
    private List<ProductImage> productImages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;


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

    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category);
    }

    public void addImage(ProductImage img) {
        productImages.add(img);
        img.setProduct(this);
    }

}
