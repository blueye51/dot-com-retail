package com.eric.store.orders.entity;

import com.eric.store.common.entity.Address;
import com.eric.store.common.util.EncryptedStringConverter;
import com.eric.store.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_orders_payment_intent", columnList = "payment_intent_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    @Column(name = "payment_intent_id", unique = true, length = 512)
    @Convert(converter = EncryptedStringConverter.class)
    private String paymentIntentId;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String currency;

    private String failureReason;

    @Column(nullable = false)
    private String shippingMethod;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal shippingCost;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "shipping_name", nullable = false, length = 512)),
            @AttributeOverride(name = "addressLine1", column = @Column(name = "shipping_address", nullable = false, length = 512)),
            @AttributeOverride(name = "addressLine2", column = @Column(name = "shipping_address2", length = 512)),
            @AttributeOverride(name = "city", column = @Column(name = "shipping_city", nullable = false, length = 512)),
            @AttributeOverride(name = "state", column = @Column(name = "shipping_state", nullable = false, length = 512)),
            @AttributeOverride(name = "zip", column = @Column(name = "shipping_zip", nullable = false, length = 512)),
            @AttributeOverride(name = "country", column = @Column(name = "shipping_country", nullable = false, length = 512)),
    })
    private Address shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
