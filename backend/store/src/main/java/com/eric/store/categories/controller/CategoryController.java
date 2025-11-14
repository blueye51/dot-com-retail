package com.eric.store.categories.controller;

import com.eric.store.categories.dto.CategoryDto;
import com.eric.store.categories.dto.CategoryRequest;
import com.eric.store.categories.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryDto> addCategory(@RequestBody CategoryRequest categoryRequest) {
        return ResponseEntity.ok(CategoryDto.from(categoryService.create(categoryRequest),
                categoryRequest.parentId()));
    }
}
