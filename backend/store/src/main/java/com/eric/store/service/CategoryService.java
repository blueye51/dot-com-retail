package com.eric.store.service;

import com.eric.store.entity.Category;
import com.eric.store.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category getProductByName(String name) {
        return categoryRepository.findByName(name).orElseThrow(() -> new RuntimeException("Category not found"));
    }
}
