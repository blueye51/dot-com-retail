package com.eric.store.products.entity;

import com.eric.store.categories.entity.Category;
import com.eric.store.orders.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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
    private String currency;

    @Column(nullable = false)
    private String name;

    private String description;

    private BigDecimal width;

    private BigDecimal height;

    private BigDecimal depth;

    private BigDecimal weight;

    @Column(nullable = false)
    private Integer stock;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

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

    /**
     * Creates a Product.
     *
     * <p><b>Invariant:</b> A Product must always be associated with a Category
     * before it is persisted. This constructor requires a non-null Category.</p>
     */
    public Product(BigDecimal price, String currency, String name, String description, BigDecimal width, BigDecimal height, BigDecimal depth, BigDecimal weight, Integer stock) {
        this.price = price;
        this.currency = currency;
        this.name = name;
        this.description = description;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.weight = weight;
        this.stock = stock;
    }

    /**
     * Not to be used directly. Use Category.addProduct(Product) instead to
     * ensure bidirectional consistency.
     * @param category
     */
    public void setCategory(Category category) {
        this.category = Objects.requireNonNull(category);
    }

    public void addImage(Image img) {
        images.add(img);
        img.setProduct(this);
    }

}
