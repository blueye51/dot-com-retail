package com.eric.store.cart.repository;

import com.eric.store.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartItem, UUID> {

    @Query("""
                SELECT ci FROM CartItem ci
                JOIN FETCH ci.product p
                LEFT JOIN FETCH p.brand
                WHERE ci.user.id = :userId
                ORDER BY ci.createdAt ASC
            """)
    List<CartItem> findByUserIdWithProduct(@Param("userId") UUID userId);

    Optional<CartItem> findByUserIdAndProductId(UUID userId, UUID productId);

    void deleteAllByUserId(UUID userId);
}
