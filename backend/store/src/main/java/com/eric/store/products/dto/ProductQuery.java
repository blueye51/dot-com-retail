package com.eric.store.products.dto;

import com.eric.store.common.util.StringUtils;
import com.eric.store.common.util.UuidUtils;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductQuery(
        String query,
        UUID categoryId,
        UUID brandId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        int page,
        int size,
        SortField sort,
        boolean descending
) {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 12;
    private static final int MAX_SIZE = 100;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String query;
        private UUID categoryId;
        private UUID brandId;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private int page = DEFAULT_PAGE;
        private int size = DEFAULT_SIZE;
        private SortField sort = SortField.CREATED_AT;
        private boolean descending = false;

        public Builder query(String query) {
            this.query = StringUtils.normalize(query);
            return this;
        }

        public Builder categoryId(String categoryId) {
            this.categoryId = UuidUtils.parseUuidOrNull(categoryId);
            return this;
        }

        public Builder brandId(String brandId) {
            this.brandId = UuidUtils.parseUuidOrNull(brandId);
            return this;
        }

        public Builder minPrice(BigDecimal minPrice) {
            this.minPrice = minPrice;
            return this;
        }

        public Builder maxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page == null ? DEFAULT_PAGE : Math.max(0, page);
            return this;
        }

        public Builder size(Integer size) {
            this.size = size == null ? DEFAULT_SIZE : Math.min(MAX_SIZE, Math.max(1, size));
            return this;
        }

        public Builder sort(String sort) {
            this.sort = SortField.fromString(sort).orElse(SortField.CREATED_AT);
            return this;
        }

        public Builder descending(boolean descending) {
            this.descending = descending;
            return this;
        }

        public ProductQuery build() {
            return new ProductQuery(query, categoryId, brandId,
                    minPrice, maxPrice, page, size, sort, descending);
        }
    }
}