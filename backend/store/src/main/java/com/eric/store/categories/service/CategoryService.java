package com.eric.store.categories.service;

import com.eric.store.categories.dto.CategoryDto;
import com.eric.store.categories.dto.CategoryRequest;
import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> CategoryDto.from(c, c.getParentCategory() != null ? c.getParentCategory().getId() : null))
                .toList();
    }

    public Category create(CategoryRequest req) {
        Category category = new Category(req.name(), req.isLeaf());

        if (req.parentId() != null) {
            Category parent = categoryRepository.findById(req.parentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found: " + req.parentId()));

            if (parent.isLeaf()) {
                throw new IllegalArgumentException("Cannot create a subcategory under a leaf category");
            }

            parent.addChild(category);
        }

        return categoryRepository.save(category);
    }

    private Category createWithParent(Category category, UUID parentId) {
        if (parentId == null) { throw new RuntimeException("Parent category not entered"); }
        Category parent = getCategoryById(parentId);
        if (parent.isLeaf()) { throw new RuntimeException("Cannot add subcategory to a leaf category"); }
        category.setParentCategory(parent);
        return categoryRepository.save(category);
    }
}
