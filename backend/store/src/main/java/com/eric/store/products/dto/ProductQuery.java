package com.eric.store.products.dto;

public record ProductQuery(
        String query,
        String categoryId,
        int page,
        int size,
        String sort,
        String order
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 100;

    public ProductQuery(String query, String categoryId, Integer page, Integer size, String sort, String order) {
        this(
                query == null ? "" : query.trim(),
                categoryId == null ? "" : categoryId.trim(),
                page == null ? DEFAULT_PAGE : Math.max(0, page),
                size == null ? DEFAULT_SIZE : Math.min(MAX_SIZE, Math.max(1, size)),
                (sort == null || sort.isBlank()) ? "createdAt" : sort.trim(),
                (order == null || order.isBlank()) ? "desc" : order.trim().toLowerCase()
        );
    }
}