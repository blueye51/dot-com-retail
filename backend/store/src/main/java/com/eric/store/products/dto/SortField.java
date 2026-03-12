package com.eric.store.products.dto;

import java.util.Optional;

public enum SortField {
    CREATED_AT("createdAt"),
    NAME("name"),
    PRICE("price");

    private final String value;

    SortField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<SortField> fromString(String sort) {
        if (sort == null || sort.isBlank()) return Optional.empty();
        for (SortField field : values()) {
            if (field.name().equalsIgnoreCase(sort) || field.value.equalsIgnoreCase(sort)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }
}
