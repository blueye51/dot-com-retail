package com.eric.store.products.repository;

import com.eric.store.products.entity.Product;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class ProductSpecification {

    public static Specification<Product> hasCategory(UUID categoryId) {
        return (root, query, cb) ->
                categoryId == null
                        ? cb.conjunction()
                        : cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Product> nameContains(String q) {
        return (root, query, cb) ->
                q == null
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%");
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
