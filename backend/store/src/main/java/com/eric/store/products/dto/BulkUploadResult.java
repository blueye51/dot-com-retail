package com.eric.store.products.dto;

import java.util.List;

public record BulkUploadResult(
        int created,
        int failed,
        List<RowError> errors
) {
    public record RowError(int row, String name, List<String> messages) {}
}
