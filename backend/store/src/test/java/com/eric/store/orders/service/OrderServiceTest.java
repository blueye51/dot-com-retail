package com.eric.store.orders.service;

import com.eric.store.cart.entity.CartItem;
import com.eric.store.cart.repository.CartRepository;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.orders.dto.CheckoutRequest;
import com.eric.store.orders.dto.CheckoutResponse;
import com.eric.store.orders.dto.OrderResponse;
import com.eric.store.orders.dto.ShippingOption;
import com.eric.store.orders.entity.Order;
import com.eric.store.orders.entity.OrderItem;
import com.eric.store.orders.entity.OrderStatus;
import com.eric.store.orders.mapper.OrderMapper;
import com.eric.store.orders.repository.OrderRepository;
import com.eric.store.payments.service.PaymentService;
import com.eric.store.products.entity.CurrencyProvider;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductRepository;
import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock CartRepository cartRepository;
    @Mock ProductRepository productRepository;
    @Mock UserService userService;
    @Mock OrderMapper orderMapper;
    @Mock PaymentService paymentService;

    @InjectMocks OrderService orderService;

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

    private CheckoutRequest makeCheckoutRequest() {
        return new CheckoutRequest("John Doe", "123 Main St", null, "Springfield", "IL", "62701", "US", false, ShippingOption.STANDARD);
    }

    private CartItem makeCartItem(User user, Product product, int quantity) {
        CartItem ci = new CartItem();
        ci.setUser(user);
        ci.setProduct(product);
        ci.setQuantity(quantity);
        return ci;
    }

    @Test
    void checkout_createsOrderWithCorrectTotal() {
        User user = makeUser();
        Product p1 = makeProduct("Widget", new BigDecimal("10.00"), 5);
        Product p2 = makeProduct("Gadget", new BigDecimal("25.50"), 10);

        List<CartItem> cart = List.of(
                makeCartItem(user, p1, 2),
                makeCartItem(user, p2, 1)
        );

        when(userService.findById(user.getId())).thenReturn(user);
        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(cart);
        when(productRepository.findById(p1.getId())).thenReturn(Optional.of(p1));
        when(productRepository.findById(p2.getId())).thenReturn(Optional.of(p2));
        when(paymentService.createPaymentIntent(any())).thenReturn("pi_secret_test");
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        CheckoutResponse response = orderService.checkout(user.getId(), makeCheckoutRequest());

        assertNotNull(response.orderId());
        assertEquals("pi_secret_test", response.clientSecret());

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();

        // 2 * 10.00 + 1 * 25.50 + 4.99 shipping = 50.49
        assertEquals(new BigDecimal("50.49"), saved.getTotalPrice());
        assertEquals(OrderStatus.PENDING_PAYMENT, saved.getStatus());
        assertEquals(2, saved.getItems().size());

        verify(cartRepository).deleteAllByUserId(user.getId());
    }

    @Test
    void checkout_emptyCart_throws() {
        User user = makeUser();
        when(userService.findById(user.getId())).thenReturn(user);
        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> orderService.checkout(user.getId(), makeCheckoutRequest()));
    }

    @Test
    void checkout_insufficientStock_throws() {
        User user = makeUser();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 1);
        List<CartItem> cart = List.of(makeCartItem(user, product, 5));

        when(userService.findById(user.getId())).thenReturn(user);
        when(cartRepository.findByUserIdWithProduct(user.getId())).thenReturn(cart);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout(user.getId(), makeCheckoutRequest()));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
    }

    @Test
    void handlePaymentSuccess_updatesStatusAndDecrementsStock() {
        UUID orderId = UUID.randomUUID();
        Product product = makeProduct("Widget", new BigDecimal("10.00"), 5);

        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("10.00"));
        order.addItem(item);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        orderService.handlePaymentSuccess(orderId, "pi_123");

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("pi_123", order.getPaymentIntentId());
        assertEquals(3, product.getStock());
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void handlePaymentSuccess_alreadyPaid_skips() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        orderService.handlePaymentSuccess(orderId, "pi_123");

        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void handlePaymentFailure_updatesStatusAndReason() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        orderService.handlePaymentFailure(orderId, "pi_456", "Insufficient funds");

        assertEquals(OrderStatus.PAYMENT_FAILED, order.getStatus());
        assertEquals("Insufficient funds", order.getFailureReason());
        verify(orderRepository).save(order);
    }

    @Test
    void cancel_pendingOrder_succeeds() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setItems(List.of());

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(
                new OrderResponse(orderId, "CANCELLED", BigDecimal.ZERO, "EUR", null, null, List.of(), null, null, null, null, null));

        OrderResponse response = orderService.cancel(orderId, userId);

        assertEquals("CANCELLED", response.status());
        verify(orderRepository).save(order);
    }

    @Test
    void cancel_paidOrder_refundsAndRestoresStock() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Product product = makeProduct("Widget", new BigDecimal("10.00"), 3);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);
        order.setPaymentIntentId("pi_123");
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("10.00"));
        order.addItem(item);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(orderMapper.toResponse(order)).thenReturn(
                new OrderResponse(orderId, "REFUNDED", BigDecimal.ZERO, "EUR", "pi_123", null, List.of(), null, null, null, null, null));

        OrderResponse response = orderService.cancel(orderId, userId);

        assertEquals("REFUNDED", response.status());
        verify(paymentService).refund("pi_123");
        assertEquals(5, product.getStock()); // 3 + 2 restored
        verify(productRepository).save(product);
        verify(orderRepository).save(order);
    }

    @Test
    void cancel_failedOrder_throws() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PAYMENT_FAILED);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.cancel(orderId, userId));
    }

    @Test
    void cancel_otherUsersOrder_throws() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        User user = new User();
        user.setId(otherUserId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> orderService.cancel(orderId, userId));
    }

    @Test
    void getById_notFound_throws() {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getById(orderId, userId));
    }
}
