package com.eric.store.products.dto;

import com.eric.store.common.util.StringUtils;
import com.eric.store.common.util.UuidUtils;

import java.util.UUID;

public record ProductQuery(
        String query,
        UUID categoryId,
        int page,
        int size,
        SortField sort,
        boolean descending
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 100;

    public ProductQuery(String query, String categoryId, Integer page, Integer size, String sort, boolean descending) {
        this(
                StringUtils.normalize(query),
                UuidUtils.parseUuidOrNull(categoryId),
                page == null ? DEFAULT_PAGE : Math.max(0, page),
                size == null ? DEFAULT_SIZE : Math.min(MAX_SIZE, Math.max(1, size)),
                SortField.fromString(sort).orElse(SortField.CREATED_AT),
                descending
        );
    }
}