package com.eric.store.entity;

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

    @NonNull
    @Column(nullable = false, updatable = false)
    private boolean isLeaf;

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "parentCategory", cascade={CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Category> subcategories = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;
}
