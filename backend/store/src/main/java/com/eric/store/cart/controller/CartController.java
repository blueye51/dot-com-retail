package com.eric.store.cart.controller;

import com.eric.store.cart.dto.CartItemRequest;
import com.eric.store.cart.dto.CartMergeRequest;
import com.eric.store.cart.dto.CartResponse;
import com.eric.store.cart.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest req, @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(req, userId));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @PathVariable UUID itemId,
            @RequestParam @Min(1) int quantity,
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.updateQuantity(itemId, quantity, userId));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable UUID itemId, @AuthenticationPrincipal UUID userId) {
        cartService.removeItem(itemId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.clear(userId));
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> merge(@Valid @RequestBody CartMergeRequest req, @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(cartService.merge(req.items(), userId));
    }
}
