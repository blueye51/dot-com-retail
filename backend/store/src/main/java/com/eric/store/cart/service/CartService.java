package com.eric.store.cart.service;

import com.eric.store.cart.dto.CartItemRequest;
import com.eric.store.cart.dto.CartResponse;
import com.eric.store.cart.entity.CartItem;
import com.eric.store.cart.mapper.CartMapper;
import com.eric.store.cart.repository.CartRepository;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductImageRepository;
import com.eric.store.products.service.ProductService;
import com.eric.store.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductService productService;
    private final UserService userService;
    private final ProductImageRepository productImageRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        List<CartItem> items = cartRepository.findByUserIdWithProduct(userId);
        Map<UUID, String> thumbnails = loadThumbnails(items);
        return cartMapper.toCartResponse(items, thumbnails);
    }

    @Transactional
    public CartResponse addItem(CartItemRequest req, UUID userId) {
        Product product = productService.findById(req.productId());

        CartItem item = cartRepository.findByUserIdAndProductId(userId, req.productId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUser(userService.findById(userId));
                    newItem.setProduct(product);
                    newItem.setQuantity(0);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + req.quantity());
        cartRepository.save(item);
        return getCart(userId);
    }

    @Transactional
    public CartResponse updateQuantity(UUID itemId, int quantity, UUID userId) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("CartItem", itemId));
        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only update your own cart");
        }
        item.setQuantity(quantity);
        cartRepository.save(item);
        return getCart(userId);
    }

    @Transactional
    public void removeItem(UUID itemId, UUID userId) {
        CartItem item = cartRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("CartItem", itemId));
        if (!item.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only remove from your own cart");
        }
        cartRepository.delete(item);
    }

    @Transactional
    public CartResponse clear(UUID userId) {
        cartRepository.deleteAllByUserId(userId);
        return new CartResponse(List.of(), java.math.BigDecimal.ZERO);
    }

    @Transactional
    public CartResponse merge(List<CartItemRequest> items, UUID userId) {
        for (CartItemRequest req : items) {
            Product product = productService.findById(req.productId());

            CartItem item = cartRepository.findByUserIdAndProductId(userId, req.productId())
                    .orElseGet(() -> {
                        CartItem newItem = new CartItem();
                        newItem.setUser(userService.findById(userId));
                        newItem.setProduct(product);
                        newItem.setQuantity(0);
                        return newItem;
                    });

            item.setQuantity(item.getQuantity() + req.quantity());
            cartRepository.save(item);
        }
        return getCart(userId);
    }

    private Map<UUID, String> loadThumbnails(List<CartItem> items) {
        List<UUID> productIds = items.stream()
                .map(item -> item.getProduct().getId())
                .toList();
        if (productIds.isEmpty()) return Map.of();

        return productImageRepository.findThumbnails(productIds).stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getId(),
                        img -> img.getFile().getUrl(),
                        (a, b) -> a
                ));
    }
}
