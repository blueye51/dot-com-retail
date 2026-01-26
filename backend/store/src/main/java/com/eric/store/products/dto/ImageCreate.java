package com.eric.store.products.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ImageCreate(
        @NotBlank String fileKey,
        @NotNull Integer sortOrder
) {

}
