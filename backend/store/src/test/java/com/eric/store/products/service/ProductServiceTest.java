package com.eric.store.products.service;

import com.eric.store.brands.entity.Brand;
import com.eric.store.categories.entity.Category;
import com.eric.store.products.dto.ProductCard;
import com.eric.store.products.dto.ProductQuery;
import com.eric.store.products.dto.SortField;
import com.eric.store.products.entity.CurrencyProvider;
import com.eric.store.products.entity.Product;
import com.eric.store.products.mapper.ProductMapper;
import com.eric.store.products.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductImageService productImageService;
    @Mock ProductMapper productMapper;

    @InjectMocks ProductService productService;

    private Product makeProduct(String name, BigDecimal price) {
        Product p = new Product();
        p.setId(UUID.randomUUID());
        p.setName(name);
        p.setPrice(price);
        p.setCurrency(CurrencyProvider.EUR);
        p.setStock(10);

        Category cat = new Category();
        cat.setName("Laptops");
        cat.addProduct(p);

        Brand brand = new Brand();
        brand.setName("TestBrand");
        p.setBrand(brand);

        return p;
    }

    @Test
    void search_noFilters_returnsEmpty() {
        ProductQuery query = ProductQuery.builder().build();
        Page<Product> fakePage = new PageImpl<>(List.of());

        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(fakePage);
        when(productImageService.loadThumbnailUrls(fakePage))
                .thenReturn(Map.of());

        Page<ProductCard> result = productService.search(query);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void search_withProducts_mapsThemToCards() {
        Product laptop = makeProduct("Galaxy Book", new BigDecimal("999.99"));

        Page<Product> fakePage = new PageImpl<>(List.of(laptop));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(fakePage);
        when(productImageService.loadThumbnailUrls(fakePage))
                .thenReturn(Map.of());

        ProductCard expectedCard = new ProductCard(
                laptop.getId(), "Galaxy Book", new BigDecimal("999.99"),
                CurrencyProvider.EUR, "TestBrand", 10, "Laptops", null
        );
        when(productMapper.toCard(eq(laptop), any())).thenReturn(expectedCard);

        Page<ProductCard> result = productService.search(ProductQuery.builder().build());

        assertEquals(1, result.getTotalElements());
        assertEquals("Galaxy Book", result.getContent().get(0).name());
        assertEquals(new BigDecimal("999.99"), result.getContent().get(0).price());
    }

    @Test
    void search_pagination_isAppliedCorrectly() {
        Page<Product> fakePage = new PageImpl<>(List.of());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(productRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(fakePage);
        when(productImageService.loadThumbnailUrls(fakePage))
                .thenReturn(Map.of());

        ProductQuery query = ProductQuery.builder().page(3).size(25).build();
        productService.search(query);

        Pageable captured = pageableCaptor.getValue();
        assertEquals(3, captured.getPageNumber());
        assertEquals(25, captured.getPageSize());
    }

    @Test
    void search_descendingPrice_appliesSortCorrectly() {
        Page<Product> fakePage = new PageImpl<>(List.of());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(productRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(fakePage);
        when(productImageService.loadThumbnailUrls(fakePage))
                .thenReturn(Map.of());

        ProductQuery query = ProductQuery.builder()
                .sort("price")
                .descending(true)
                .build();
        productService.search(query);

        Pageable captured = pageableCaptor.getValue();
        assertEquals("price: DESC", captured.getSort().toString());
    }

    @Test
    void search_ascendingName_appliesSortCorrectly() {
        Page<Product> fakePage = new PageImpl<>(List.of());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(productRepository.findAll(any(Specification.class), pageableCaptor.capture()))
                .thenReturn(fakePage);
        when(productImageService.loadThumbnailUrls(fakePage))
                .thenReturn(Map.of());

        ProductQuery query = ProductQuery.builder()
                .sort("name")
                .descending(false)
                .build();
        productService.search(query);

        Pageable captured = pageableCaptor.getValue();
        assertEquals("name: ASC", captured.getSort().toString());
    }

    @Test
    void search_thumbnails_areMappedToCorrectProducts() {
        Product p1 = makeProduct("Product A", new BigDecimal("100"));
        Product p2 = makeProduct("Product B", new BigDecimal("200"));
        String thumbnailA = "http://localhost:9000/images/public/a.webp";

        Page<Product> fakePage = new PageImpl<>(List.of(p1, p2));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(fakePage);

        when(productImageService.loadThumbnailUrls(fakePage))
                .thenReturn(Map.of(p1.getId(), thumbnailA));

        when(productMapper.toCard(any(), any())).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            String thumb = inv.getArgument(1);
            return new ProductCard(p.getId(), p.getName(), p.getPrice(),
                    p.getCurrency(), "TestBrand", 10, "Laptops", thumb);
        });

        Page<ProductCard> result = productService.search(ProductQuery.builder().build());

        assertEquals(2, result.getTotalElements());

        assertEquals(thumbnailA, result.getContent().get(0).imageUrl());

        assertNull(result.getContent().get(1).imageUrl());

        verify(productMapper).toCard(eq(p1), eq(thumbnailA));
        verify(productMapper).toCard(eq(p2), isNull());
    }
}
