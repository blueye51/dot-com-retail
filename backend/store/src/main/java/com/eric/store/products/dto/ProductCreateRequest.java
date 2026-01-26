package com.eric.store.products.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductCreateRequest(
        @NotBlank String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        String description,
        @DecimalMin("0.0") BigDecimal width,
        @DecimalMin("0.0") BigDecimal height,
        @DecimalMin("0.0") BigDecimal depth,
        @DecimalMin("0.0") BigDecimal weight,
        @NotNull Integer stock,
        @NotNull UUID categoryId,
        @Valid List<ImageCreate> images
) {
}
