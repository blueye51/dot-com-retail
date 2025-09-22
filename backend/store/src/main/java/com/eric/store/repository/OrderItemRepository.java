package com.eric.store.repository;

import com.eric.store.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<Order, UUID> {
}
