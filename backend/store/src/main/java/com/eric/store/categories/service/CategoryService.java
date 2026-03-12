package com.eric.store.categories.service;

import com.eric.store.categories.dto.CategoryDto;
import com.eric.store.categories.dto.CategoryRequest;
import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.common.exceptions.IllegalJsonException;
import com.eric.store.common.exceptions.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
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
        return categoryRepository.findByName(name).orElseThrow(() -> new NotFoundException("Parent category name not found", name));
    }

    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category not found", id));
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
                    .orElseThrow(() -> new NotFoundException("Parent category not found", req.parentId()));

            if (parent.isLeaf()) {
                throw new IllegalJsonException("Cannot create a subcategory under a leaf category");
            }

            parent.addChild(category);
        }

        return categoryRepository.save(category);
    }

    public CategoryDto rename(UUID id, String name) {
        Category category = getCategoryById(id);
        category.setName(name);
        Category saved = categoryRepository.save(category);
        return CategoryDto.from(saved, saved.getParentCategory() != null ? saved.getParentCategory().getId() : null);
    }

    public void delete(UUID id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}
