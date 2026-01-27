package com.eric.store.products.repository;

import com.eric.store.products.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    List<Image> findAllByProductIdOrderBySortOrderAsc(UUID product_id);
}
