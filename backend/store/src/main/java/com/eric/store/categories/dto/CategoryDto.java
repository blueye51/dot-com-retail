package com.eric.store.categories.dto;

import com.eric.store.categories.entity.Category;

import java.util.UUID;

public record CategoryDto(
        UUID id,
        String name,
        Boolean isLeaf,
        UUID parentId
) {
    public static CategoryDto from(Category c, UUID parentId) {
        return new CategoryDto(
                c.getId(),
                c.getName(),
                c.isLeaf(),
                parentId
        );
    }
}
