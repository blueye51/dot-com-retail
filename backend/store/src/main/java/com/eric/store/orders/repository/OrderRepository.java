package com.eric.store.orders.repository;

import com.eric.store.orders.entity.Order;
import com.eric.store.orders.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
                SELECT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.product
                WHERE o.id = :id
            """)
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @Query("""
                SELECT o FROM Order o
                LEFT JOIN FETCH o.items
                WHERE o.user.id = :userId
                  AND (CAST(:status AS string) IS NULL OR o.status = :status)
                  AND (CAST(:from AS timestamp) IS NULL OR o.createdAt >= :from)
                  AND (CAST(:to AS timestamp) IS NULL OR o.createdAt <= :to)
                ORDER BY o.createdAt DESC
            """)
    Page<Order> findByUserIdFiltered(
            @Param("userId") UUID userId,
            @Param("status") OrderStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable);

    @Query("""
                SELECT o FROM Order o
                LEFT JOIN FETCH o.items
                JOIN FETCH o.user
                WHERE (CAST(:status AS string) IS NULL OR o.status = :status)
                  AND (CAST(:from AS timestamp) IS NULL OR o.createdAt >= :from)
                  AND (CAST(:to AS timestamp) IS NULL OR o.createdAt <= :to)
                ORDER BY o.createdAt DESC
            """)
    Page<Order> findAllFiltered(
            @Param("status") OrderStatus status,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable);

    Optional<Order> findByPaymentIntentId(String paymentIntentId);

    @Query("""
                SELECT o FROM Order o
                JOIN FETCH o.user
                WHERE o.id = :id
            """)
    Optional<Order> findByIdWithUser(@Param("id") UUID id);
}
