package com.eric.store.categories.entity;

import com.eric.store.products.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_name", columnList = "name"),
        }
)
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class Category {

    @Id
    @GeneratedValue
    private UUID id;

    @NonNull
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, updatable = false)
    private boolean isLeaf;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "parentCategory", cascade={CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Category> subcategories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    public Category(String name, boolean isLeaf) {
        this.name = name;
        this.isLeaf = isLeaf;
    }

    public void addChild(Category child) {
        if (this.isLeaf) {
            throw new IllegalStateException("Cannot add subcategories under a leaf category");
        }
        child.parentCategory = this;
        this.subcategories.add(child);
    }

    public void removeChild(Category child) {
        this.subcategories.remove(child);
        child.parentCategory = null;
    }
}
