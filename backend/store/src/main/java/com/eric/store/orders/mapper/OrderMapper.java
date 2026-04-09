package com.eric.store.orders.mapper;

import com.eric.store.common.entity.Address;
import com.eric.store.orders.dto.AddressResponse;
import com.eric.store.orders.dto.AdminOrderSummary;
import com.eric.store.orders.dto.OrderItemResponse;
import com.eric.store.orders.dto.OrderResponse;
import com.eric.store.orders.dto.OrderSummary;
import com.eric.store.orders.entity.Order;
import com.eric.store.orders.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getPrice(),
                item.getQuantity(),
                item.getTotalPrice()
        );
    }

    /**
     * Maps an Order to OrderResponse.
     * The Order must be fetched with items and their products initialized.
     */
    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getCurrency(),
                order.getPaymentIntentId(),
                order.getFailureReason(),
                order.getItems().stream().map(this::toItemResponse).toList(),
                toAddressResponse(order.getShippingAddress()),
                order.getShippingMethod(),
                order.getShippingCost(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private AddressResponse toAddressResponse(Address a) {
        if (a == null) return null;
        return new AddressResponse(
                a.getName(), a.getAddressLine1(), a.getAddressLine2(),
                a.getCity(), a.getState(), a.getZip(), a.getCountry()
        );
    }

    public OrderSummary toSummary(Order order) {
        return new OrderSummary(
                order.getId(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getCurrency(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }

    public AdminOrderSummary toAdminSummary(Order order) {
        return new AdminOrderSummary(
                order.getId(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getCurrency(),
                order.getItems().size(),
                order.getUser().getEmail(),
                order.getUser().getName(),
                order.getCreatedAt()
        );
    }
}
