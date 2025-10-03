package com.eric.store.service;

import com.eric.store.entity.Category;
import com.eric.store.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Category createLeaf(String name, UUID parentId) {
        Category category = new Category(name, true);
        return createWithParent(category, parentId);
    }

    public Category createRoot(String name) {
        Category category = new Category(name, false);
        return categoryRepository.save(category);
    }

    public Category createSub(String name, UUID parentId) {
        Category category = new Category(name, false);
        return createWithParent(category, parentId);
    }

    private Category createWithParent(Category category, UUID parentId) {
        if (parentId == null) { throw new RuntimeException("Parent category not entered"); }
        Category parent = getCategoryById(parentId);
        if (parent.isLeaf()) { throw new RuntimeException("Cannot add subcategory to a leaf category"); }
        category.setParentCategory(parent);
        return categoryRepository.save(category);
    }
}
