package com.eric.store.products.repository;

import com.eric.store.products.entity.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductSpecification {

    public static Specification<Product> hasCategory(UUID categoryId) {
        if (categoryId == null) return Specification.where(null);
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> hasBrand(UUID brandId) {
        if (brandId == null) return Specification.where(null);
        return (root, query, cb) -> cb.equal(root.get("brand").get("id"), brandId);
    }

    public static Specification<Product> nameContains(String q) {
        if (q == null) return Specification.where(null);
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        if (minPrice == null) return Specification.where(null);
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        if (maxPrice == null) return Specification.where(null);
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Product> fetchBrand() {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("brand", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Product> fetchCategory() {
        return (root, query, cb) -> {
            if (query != null && !Long.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }
}
