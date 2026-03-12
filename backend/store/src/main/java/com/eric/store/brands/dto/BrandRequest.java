package com.eric.store.brands.dto;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest(@NotBlank String name) {}
