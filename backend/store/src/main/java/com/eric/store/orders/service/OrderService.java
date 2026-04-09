package com.eric.store.orders.service;

import com.eric.store.cart.entity.CartItem;
import com.eric.store.cart.repository.CartRepository;
import com.eric.store.common.entity.Address;
import com.eric.store.common.exceptions.NotFoundException;
import com.eric.store.orders.dto.*;
import com.eric.store.orders.dto.ShippingOption;
import com.eric.store.orders.entity.Order;
import com.eric.store.orders.entity.OrderItem;
import com.eric.store.orders.entity.OrderStatus;
import com.eric.store.orders.mapper.OrderMapper;
import com.eric.store.orders.repository.OrderRepository;
import com.eric.store.payments.service.PaymentService;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductRepository;
import com.eric.store.user.entity.User;
import com.eric.store.user.service.UserService;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final OrderMapper orderMapper;
    private final PaymentService paymentService;

    /**
     * Checkout: create order from cart, create Stripe PaymentIntent, clear cart.
     * Returns the Stripe client secret for the frontend to confirm payment.
     */
    @Transactional
    public CheckoutResponse checkout(UUID userId, CheckoutRequest req) {
        User user = userService.findById(userId);
        List<CartItem> cartItems = cartRepository.findByUserIdWithProduct(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Validate stock (re-fetch for latest version to avoid stale reads)
        for (CartItem ci : cartItems) {
            Product product = productRepository.findById(ci.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product", ci.getProduct().getId()));
            ci.setProduct(product);
            if (product.getStock() < ci.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for " + product.getName() +
                        " (available: " + product.getStock() + ", requested: " + ci.getQuantity() + ")");
            }
        }

        // Determine currency from first product (all items must share currency in a real store)
        String currency = cartItems.get(0).getProduct().getCurrency().toString();

        // Build order
        Address address = new Address(
                req.name(), req.addressLine1(), req.addressLine2(),
                req.city(), req.state(), req.zip(), req.country()
        );

        ShippingOption shipping = req.shippingOption();

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setCurrency(currency);
        order.setShippingAddress(address);
        order.setShippingMethod(shipping.getLabel());
        order.setShippingCost(shipping.getCost());

        // Save address to user profile if requested
        if (req.saveAddress()) {
            user.setSavedAddress(address);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProduct(ci.getProduct());
            oi.setPrice(ci.getProduct().getPrice());
            oi.setQuantity(ci.getQuantity());
            order.addItem(oi);

            subtotal = subtotal.add(
                    ci.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(ci.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP));
        }
        order.setTotalPrice(subtotal.add(shipping.getCost()));

        Order saved = orderRepository.save(order);

        // Create Stripe PaymentIntent
        String clientSecret = paymentService.createPaymentIntent(saved);

        // Clear the cart
        cartRepository.deleteAllByUserId(userId);

        return new CheckoutResponse(saved.getId(), clientSecret);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only view your own orders");
        }
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummary> getOrderHistory(UUID userId, String status, OffsetDateTime from, OffsetDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        OrderStatus orderStatus = (status != null && !status.isBlank()) ? OrderStatus.valueOf(status) : null;
        Page<Order> orders = orderRepository.findByUserIdFiltered(userId, orderStatus, from, to, pageable);
        return orders.map(orderMapper::toSummary);
    }

    @Transactional
    public OrderResponse cancel(UUID orderId, UUID userId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only cancel your own orders");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            // Refund via Stripe and restore inventory
            paymentService.refund(order.getPaymentIntentId());
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProduct().getId())
                        .orElseThrow(() -> new NotFoundException("Product", item.getProduct().getId()));
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
            log.info("Order {} refunded and inventory restored", orderId);
        } else if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("This order cannot be cancelled");
        }

        return orderMapper.toResponse(order);
    }

    // ── Admin methods ──

    @Transactional(readOnly = true)
    public Page<AdminOrderSummary> getAllOrders(String status, OffsetDateTime from, OffsetDateTime to, int page, int size) {
        OrderStatus orderStatus = (status != null && !status.isBlank()) ? OrderStatus.valueOf(status) : null;
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllFiltered(orderStatus, from, to, pageable);
        return orders.map(orderMapper::toAdminSummary);
    }

    @Transactional(readOnly = true)
    public OrderResponse getByIdAdmin(UUID orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, String newStatus) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        OrderStatus target = OrderStatus.valueOf(newStatus);
        order.setStatus(target);

        // If admin refunds, also trigger Stripe refund and restore inventory
        if (target == OrderStatus.REFUNDED && order.getPaymentIntentId() != null) {
            paymentService.refund(order.getPaymentIntentId());
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProduct().getId())
                        .orElseThrow(() -> new NotFoundException("Product", item.getProduct().getId()));
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
            log.info("Admin refunded order {} and restored inventory", orderId);
        }

        orderRepository.save(order);
        return orderMapper.toResponse(order);
    }

    /**
     * Called by the RabbitMQ consumer when payment succeeds.
     * Updates status and decrements inventory atomically.
     */
    @Transactional
    public void handlePaymentSuccess(UUID orderId, String paymentIntentId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Order {} already processed (status={}), skipping", orderId, order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaymentIntentId(paymentIntentId);

        // Decrement stock with optimistic locking — if another transaction modified
        // the product concurrently, the @Version check will cause a retry
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product", item.getProduct().getId()));
            int newStock = product.getStock() - item.getQuantity();
            if (newStock < 0) {
                log.error("Inventory underflow for product {} on order {}", product.getId(), orderId);
                newStock = 0;
            }
            product.setStock(newStock);
            productRepository.save(product);
        }

        orderRepository.save(order);
        log.info("Order {} marked as PAID, inventory decremented", orderId);
    }

    /**
     * Called by the RabbitMQ consumer when payment fails.
     */
    @Transactional
    public void handlePaymentFailure(UUID orderId, String paymentIntentId, String reason) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            log.warn("Order {} already processed (status={}), skipping", orderId, order.getStatus());
            return;
        }

        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setPaymentIntentId(paymentIntentId);
        order.setFailureReason(reason);
        orderRepository.save(order);
        log.info("Order {} marked as PAYMENT_FAILED: {}", orderId, reason);
    }

    /**
     * Used by the consumer to look up the user's email for notifications.
     */
    @Transactional(readOnly = true)
    public Order findByIdWithUser(UUID orderId) {
        return orderRepository.findByIdWithUser(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
    }
}
