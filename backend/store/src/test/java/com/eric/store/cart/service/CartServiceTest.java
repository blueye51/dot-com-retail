package com.eric.store.cart.service;

import com.eric.store.cart.dto.CartItemRequest;
import com.eric.store.cart.dto.CartResponse;
import com.eric.store.cart.entity.CartItem;
import com.eric.store.cart.mapper.CartMapper;
import com.eric.store.cart.repository.CartRepository;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.products.entity.CurrencyProvider;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductImageRepository;
import com.eric.store.products.service.ProductService;
import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock CartRepository cartRepository;
    @Spy  CartMapper cartMapper;
    @Mock ProductService productService;
    @Mock UserService userService;
    @Mock ProductImageRepository productImageRepository;

    @InjectMocks CartService cartService;

    private User makeUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@test.com");
        return user;
    }

    private Product makeProduct(String name, BigDecimal price, int stock) {
        Product p = new Product();
        p.setId(UUID.randomUUID());
        p.setName(name);
        p.setPrice(price);
        p.setCurrency(CurrencyProvider.EUR);
        p.setStock(stock);
        return p;
    }

    private CartItem makeCartItem(User user, Product product, int quantity) {
        CartItem ci = new CartItem();
        ci.setId(UUID.randomUUID());
        ci.setUser(user);
        ci.setProduct(product);
        ci.setQuantity(quantity);
        return ci;
    }

    // ---- getCart ----

    @Test
    void getCart_returnsItemsAndCorrectTotal() {
        User user = makeUser();
        Product p1 = makeProduct("Widget", new BigDecimal("10.00"), 5);
        Product p2 = makeProduct("Gadget", new BigDecimal("25.50"), 10);

        List<CartItem> items = List.of(
                makeCartItem(user, p1, 2),
                makeCartItem(user, p2, 3)
        );

        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(items);
        when(productImageRepository.findThumbnails(any())).thenReturn(List.of());

        CartResponse response = cartService.getCart(user.getId());

        assertEquals(2, response.items().size());
        // 2 * 10.00 + 3 * 25.50 = 96.50
        assertEquals(new BigDecimal("96.50"), response.total());
    }

    @Test
    void getCart_emptyCart_returnsZeroTotal() {
        User user = makeUser();

        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(List.of());

        CartResponse response = cartService.getCart(user.getId());

        assertTrue(response.items().isEmpty());
        assertEquals(BigDecimal.ZERO, response.total());
    }

    @Test
    void getCart_singleItem_totalEqualsLineTotal() {
        User user = makeUser();
        Product p = makeProduct("Widget", new BigDecimal("7.99"), 10);
        List<CartItem> items = List.of(makeCartItem(user, p, 4));

        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(items);
        when(productImageRepository.findThumbnails(any())).thenReturn(List.of());

        CartResponse response = cartService.getCart(user.getId());

        // 4 * 7.99 = 31.96
        assertEquals(new BigDecimal("31.96"), response.total());
    }

    // ---- addItem ----

    @Test
    void addItem_newProduct_createsNewCartItem() {
        User user = makeUser();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 5);
        CartItemRequest req = new CartItemRequest(product.getId(), 2);

        when(productService.findById(product.getId())).thenReturn(product);
        when(cartRepository.findByUserIdAndProductId(user.getId(), product.getId())).thenReturn(Optional.empty());
        when(userService.findById(user.getId())).thenReturn(user);
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(List.of());

        cartService.addItem(req, user.getId());

        verify(cartRepository).save(argThat(item -> item.getQuantity() == 2));
    }

    @Test
    void addItem_existingProduct_incrementsQuantity() {
        User user = makeUser();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 5);
        CartItem existing = makeCartItem(user, product, 3);
        CartItemRequest req = new CartItemRequest(product.getId(), 2);

        when(productService.findById(product.getId())).thenReturn(product);
        when(cartRepository.findByUserIdAndProductId(user.getId(), product.getId())).thenReturn(Optional.of(existing));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(List.of());

        cartService.addItem(req, user.getId());

        verify(cartRepository).save(argThat(item -> item.getQuantity() == 5));
    }

    // ---- updateQuantity ----

    @Test
    void updateQuantity_updatesExistingItem() {
        User user = makeUser();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 5);
        CartItem item = makeCartItem(user, product, 2);

        when(cartRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(List.of());

        cartService.updateQuantity(item.getId(), 5, user.getId());

        assertEquals(5, item.getQuantity());
        verify(cartRepository).save(item);
    }

    @Test
    void updateQuantity_otherUsersItem_throws() {
        User user = makeUser();
        User otherUser = makeUser();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 5);
        CartItem item = makeCartItem(otherUser, product, 2);

        when(cartRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.updateQuantity(item.getId(), 5, user.getId()));
    }

    @Test
    void updateQuantity_notFound_throws() {
        UUID itemId = UUID.randomUUID();
        when(cartRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> cartService.updateQuantity(itemId, 5, UUID.randomUUID()));
    }

    // ---- removeItem ----

    @Test
    void removeItem_deletesItem() {
        User user = makeUser();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 5);
        CartItem item = makeCartItem(user, product, 2);

        when(cartRepository.findById(item.getId())).thenReturn(Optional.of(item));

        cartService.removeItem(item.getId(), user.getId());

        verify(cartRepository).delete(item);
    }

    @Test
    void removeItem_otherUsersItem_throws() {
        User user = makeUser();
        User otherUser = makeUser();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 5);
        CartItem item = makeCartItem(otherUser, product, 2);

        when(cartRepository.findById(item.getId())).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> cartService.removeItem(item.getId(), user.getId()));
        verify(cartRepository, never()).delete(any());
    }

    // ---- clear ----

    @Test
    void clear_deletesAllAndReturnsEmptyCart() {
        User user = makeUser();

        CartResponse response = cartService.clear(user.getId());

        verify(cartRepository).deleteAllByUserId(user.getId());
        assertTrue(response.items().isEmpty());
        assertEquals(BigDecimal.ZERO, response.total());
    }

    // ---- merge ----

    @Test
    void merge_combinesGuestAndServerCart() {
        User user = makeUser();
        Product p1 = makeProduct("Widget", new BigDecimal("10.00"), 5);
        Product p2 = makeProduct("Gadget", new BigDecimal("25.00"), 10);

        CartItem existingItem = makeCartItem(user, p1, 1);

        List<CartItemRequest> guestItems = List.of(
                new CartItemRequest(p1.getId(), 2),
                new CartItemRequest(p2.getId(), 3)
        );

        when(productService.findById(p1.getId())).thenReturn(p1);
        when(productService.findById(p2.getId())).thenReturn(p2);
        when(cartRepository.findByUserIdAndProductId(user.getId(), p1.getId())).thenReturn(Optional.of(existingItem));
        when(cartRepository.findByUserIdAndProductId(user.getId(), p2.getId())).thenReturn(Optional.empty());
        when(userService.findById(user.getId())).thenReturn(user);
        when(cartRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(List.of());

        cartService.merge(guestItems, user.getId());

        // p1: existing 1 + guest 2 = 3
        verify(cartRepository).save(argThat(item ->
                item.getProduct().getId().equals(p1.getId()) && item.getQuantity() == 3));
        // p2: new with qty 3
        verify(cartRepository).save(argThat(item ->
                item.getProduct().getId().equals(p2.getId()) && item.getQuantity() == 3));
    }
}
