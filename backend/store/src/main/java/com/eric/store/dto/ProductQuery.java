package com.eric.store.dto;

import org.springframework.data.domain.Sort;

public record ProductQuery(
        String query,
        String category,
        int page,
        int size,
        String sort,
        String order
) {
    public ProductQuery(String query, String category, Integer page, Integer size, String sort, String order) {
        this(
                query == null ? "" : query,
                category == null ? "" : category,
                page == null ? 0 : page,
                size == null ? 12 : size,
                (sort == null || sort.isBlank()) ? "createdAt" : sort,
                (order == null || order.isBlank()) ? "desc" : order
        );
    }
}