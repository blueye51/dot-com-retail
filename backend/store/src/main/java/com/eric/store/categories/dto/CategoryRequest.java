package com.eric.store.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CategoryRequest(
        @NotBlank String name,
        @NotNull Boolean isLeaf,
        UUID parentId
) {}