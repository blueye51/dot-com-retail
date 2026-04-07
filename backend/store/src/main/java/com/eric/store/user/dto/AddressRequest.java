package com.eric.store.user.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String name,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String zip,
        @NotBlank String country
) {
}
