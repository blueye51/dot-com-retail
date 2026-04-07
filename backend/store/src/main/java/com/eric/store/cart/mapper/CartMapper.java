package com.eric.store.cart.mapper;

import com.eric.store.cart.dto.CartItemResponse;
import com.eric.store.cart.dto.CartResponse;
import com.eric.store.cart.entity.CartItem;
import com.eric.store.products.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CartMapper {

    /**
     * Maps a CartItem to CartItemResponse.
     * The CartItem must be fetched with JOIN FETCH on product and product.brand.
     */
    public CartItemResponse toItemResponse(CartItem item, String imageUrl) {
        Product product = item.getProduct();
        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCurrency().toString(),
                item.getQuantity(),
                product.getStock(),
                imageUrl
        );
    }

    public CartResponse toCartResponse(List<CartItem> items, Map<UUID, String> thumbnails) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> toItemResponse(item, thumbnails.get(item.getProduct().getId())))
                .toList();

        BigDecimal total = items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(itemResponses, total);
    }
}
