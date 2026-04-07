package com.eric.store.orders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotBlank String name,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String zip,
        @NotBlank String country,
        boolean saveAddress,
        @NotNull ShippingOption shippingOption
) {
}
