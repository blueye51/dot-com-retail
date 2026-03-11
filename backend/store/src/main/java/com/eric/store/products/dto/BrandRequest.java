package com.eric.store.products.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(@NotBlank String name) {}
