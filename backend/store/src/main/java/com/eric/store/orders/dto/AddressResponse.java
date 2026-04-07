package com.eric.store.orders.dto;

public record AddressResponse(
        String name,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String zip,
        String country
) {
}
