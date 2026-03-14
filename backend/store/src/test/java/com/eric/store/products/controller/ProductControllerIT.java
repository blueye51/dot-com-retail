package com.eric.store.products.controller;

import com.eric.store.TestcontainersConfig;
import com.eric.store.brands.entity.Brand;
import com.eric.store.brands.repository.BrandRepository;
import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.products.entity.CurrencyProvider;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfig.class)
class ProductControllerIT {

    @Autowired MockMvc mockMvc;
    @Autowired ProductRepository productRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired BrandRepository brandRepository;

    private Category category;
    private Brand brand;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();

        category = new Category();
        category.setName("Laptops");
        category.setLeaf(true);
        category = categoryRepository.save(category);

        brand = new Brand();
        brand.setName("Samsung");
        brand = brandRepository.save(brand);
    }

    private Product createProduct(String name, BigDecimal price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setCurrency(CurrencyProvider.EUR);
        p.setStock(10);
        p.setBrand(brand);
        p.setCategory(category);
        return productRepository.save(p);
    }

    @Test
    void getPage_noFilters_returnsAllProducts() throws Exception {
        createProduct("Galaxy Book", new BigDecimal("999.99"));
        createProduct("MacBook Pro", new BigDecimal("1999.99"));

        mockMvc.perform(get("/api/products/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getPage_searchByName_filtersResults() throws Exception {
        createProduct("Galaxy Book", new BigDecimal("999.99"));
        createProduct("MacBook Pro", new BigDecimal("1999.99"));

        mockMvc.perform(get("/api/products/page").param("search", "galaxy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Galaxy Book"));
    }

    @Test
    void getPage_priceRange_filtersResults() throws Exception {
        createProduct("Budget Laptop", new BigDecimal("499.99"));
        createProduct("Premium Laptop", new BigDecimal("1999.99"));

        mockMvc.perform(get("/api/products/page")
                        .param("minPrice", "1000")
                        .param("maxPrice", "2000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Premium Laptop"));
    }

    @Test
    void getPage_filterByCategory_filtersResults() throws Exception {
        createProduct("Samsung Laptop", new BigDecimal("999.99"));

        Category phones = new Category();
        phones.setName("Phones");
        phones.setLeaf(true);
        phones = categoryRepository.save(phones);

        Product phone = new Product();
        phone.setName("iPhone");
        phone.setPrice(new BigDecimal("1099.99"));
        phone.setCurrency(CurrencyProvider.EUR);
        phone.setStock(5);
        phone.setCategory(phones);
        productRepository.save(phone);

        mockMvc.perform(get("/api/products/page")
                        .param("categoryId", category.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Samsung Laptop"));
    }

    @Test
    void getPage_filterByBrand_filtersResults() throws Exception {
        createProduct("Samsung Laptop", new BigDecimal("999.99"));

        Brand apple = new Brand();
        apple.setName("Apple");
        apple = brandRepository.save(apple);

        Product macbook = new Product();
        macbook.setName("MacBook Pro");
        macbook.setPrice(new BigDecimal("1999.99"));
        macbook.setCurrency(CurrencyProvider.EUR);
        macbook.setStock(5);
        macbook.setBrand(apple);
        macbook.setCategory(category);
        productRepository.save(macbook);

        mockMvc.perform(get("/api/products/page")
                        .param("brandId", brand.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Samsung Laptop"));
    }

    @Test
    void getPage_sortByPriceAsc_returnsOrdered() throws Exception {
        createProduct("Expensive", new BigDecimal("2000"));
        createProduct("Cheap", new BigDecimal("500"));
        createProduct("Mid", new BigDecimal("1000"));

        mockMvc.perform(get("/api/products/page")
                        .param("sort", "price")
                        .param("descending", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Cheap"))
                .andExpect(jsonPath("$.content[1].name").value("Mid"))
                .andExpect(jsonPath("$.content[2].name").value("Expensive"));
    }

    @Test
    void getPage_pagination_respectsPageAndSize() throws Exception {
        for (int i = 0; i < 5; i++) {
            createProduct("Product " + i, new BigDecimal(100 + i));
        }

        mockMvc.perform(get("/api/products/page")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void getPage_noMatchingProducts_returnsEmpty() throws Exception {
        createProduct("Galaxy Book", new BigDecimal("999.99"));

        mockMvc.perform(get("/api/products/page").param("search", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
