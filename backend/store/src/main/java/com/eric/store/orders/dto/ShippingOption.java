package com.eric.store.orders.dto;

import java.math.BigDecimal;

public enum ShippingOption {
    STANDARD("Standard Shipping (5-7 days)", new BigDecimal("4.99")),
    EXPRESS("Express Shipping (2-3 days)", new BigDecimal("14.99"));

    private final String label;
    private final BigDecimal cost;

    ShippingOption(String label, BigDecimal cost) {
        this.label = label;
        this.cost = cost;
    }

    public String getLabel() { return label; }
    public BigDecimal getCost() { return cost; }
}
