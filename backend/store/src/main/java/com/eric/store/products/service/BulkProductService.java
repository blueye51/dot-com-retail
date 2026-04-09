package com.eric.store.products.service;

import com.eric.store.brands.entity.Brand;
import com.eric.store.brands.repository.BrandRepository;
import com.eric.store.categories.entity.Category;
import com.eric.store.categories.repository.CategoryRepository;
import com.eric.store.products.dto.BulkUploadResult;
import com.eric.store.products.dto.BulkUploadResult.RowError;
import com.eric.store.products.entity.CurrencyProvider;
import com.eric.store.products.entity.Product;
import com.eric.store.products.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public BulkUploadResult upload(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        List<Map<String, String>> rows;
        if (filename.endsWith(".csv")) {
            rows = parseCsv(file);
        } else if (filename.endsWith(".json")) {
            rows = parseJson(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Use .csv or .json");
        }

        int created = 0;
        List<RowError> errors = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            int rowNum = i + 1;
            String rowName = getOrDefault(row, "name", "");

            List<String> messages = new ArrayList<>();
            validate(row, messages);

            if (!messages.isEmpty()) {
                errors.add(new RowError(rowNum, rowName, messages));
                continue;
            }

            try {
                Product product = buildProduct(row, messages);
                if (!messages.isEmpty()) {
                    errors.add(new RowError(rowNum, rowName, messages));
                    continue;
                }
                productRepository.save(product);
                created++;
            } catch (Exception e) {
                errors.add(new RowError(rowNum, rowName, List.of("Unexpected error: " + e.getMessage())));
            }
        }

        return new BulkUploadResult(created, errors.size(), errors);
    }

    private void validate(Map<String, String> row, List<String> errors) {
        String name = getOrDefault(row, "name", "").trim();
        if (name.isBlank()) errors.add("'name' is required");

        String priceStr = getOrDefault(row, "price", "").trim();
        if (priceStr.isBlank()) {
            errors.add("'price' is required");
        } else {
            try {
                BigDecimal price = new BigDecimal(priceStr);
                if (price.compareTo(BigDecimal.ZERO) < 0) errors.add("'price' must be >= 0");
            } catch (NumberFormatException e) {
                errors.add("'price' is not a valid number: " + priceStr);
            }
        }

        String currency = getOrDefault(row, "currency", "").trim();
        if (currency.isBlank()) {
            errors.add("'currency' is required");
        } else {
            try {
                CurrencyProvider.valueOf(currency.toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("'currency' is not valid: " + currency + ". Use: " + Arrays.toString(CurrencyProvider.values()));
            }
        }

        String stockStr = getOrDefault(row, "stock", "").trim();
        if (stockStr.isBlank()) {
            errors.add("'stock' is required");
        } else {
            try {
                int stock = Integer.parseInt(stockStr);
                if (stock < 0) errors.add("'stock' must be >= 0");
            } catch (NumberFormatException e) {
                errors.add("'stock' is not a valid integer: " + stockStr);
            }
        }

        String category = getOrDefault(row, "category", "").trim();
        if (category.isBlank()) {
            errors.add("'category' is required");
        } else if (categoryRepository.findByName(category).isEmpty()) {
            errors.add("Category not found: '" + category + "'");
        }

        String brand = getOrDefault(row, "brand", "").trim();
        if (!brand.isBlank() && brandRepository.findByName(brand).isEmpty()) {
            errors.add("Brand not found: '" + brand + "'");
        }

        validateOptionalDecimal(row, "width", errors);
        validateOptionalDecimal(row, "height", errors);
        validateOptionalDecimal(row, "depth", errors);
        validateOptionalDecimal(row, "weight", errors);
    }

    private void validateOptionalDecimal(Map<String, String> row, String field, List<String> errors) {
        String val = getOrDefault(row, field, "").trim();
        if (val.isBlank()) return;
        try {
            BigDecimal bd = new BigDecimal(val);
            if (bd.compareTo(BigDecimal.ZERO) < 0) errors.add("'" + field + "' must be >= 0");
        } catch (NumberFormatException e) {
            errors.add("'" + field + "' is not a valid number: " + val);
        }
    }

    private Product buildProduct(Map<String, String> row, List<String> errors) {
        Product product = new Product();
        product.setName(getOrDefault(row, "name", "").trim());
        product.setPrice(new BigDecimal(getOrDefault(row, "price", "0").trim()));
        product.setCurrency(CurrencyProvider.valueOf(getOrDefault(row, "currency", "EUR").trim().toUpperCase()));
        product.setStock(Integer.parseInt(getOrDefault(row, "stock", "0").trim()));

        String desc = getOrDefault(row, "description", "").trim();
        if (!desc.isBlank()) product.setDescription(desc);

        setOptionalDecimal(row, "width", product::setWidth);
        setOptionalDecimal(row, "height", product::setHeight);
        setOptionalDecimal(row, "depth", product::setDepth);
        setOptionalDecimal(row, "weight", product::setWeight);

        String categoryName = getOrDefault(row, "category", "").trim();
        Category category = categoryRepository.findByName(categoryName).orElse(null);
        if (category == null) {
            errors.add("Category not found: '" + categoryName + "'");
            return product;
        }
        product.setCategory(category);

        String brandName = getOrDefault(row, "brand", "").trim();
        if (!brandName.isBlank()) {
            Brand brand = brandRepository.findByName(brandName).orElse(null);
            if (brand == null) {
                errors.add("Brand not found: '" + brandName + "'");
                return product;
            }
            product.setBrand(brand);
        }

        return product;
    }

    private void setOptionalDecimal(Map<String, String> row, String field, java.util.function.Consumer<BigDecimal> setter) {
        String val = getOrDefault(row, field, "").trim();
        if (!val.isBlank()) {
            setter.accept(new BigDecimal(val));
        }
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                record.toMap().forEach((k, v) -> row.put(k.toLowerCase(), v));
                rows.add(row);
            }
        }
        return rows;
    }

    private List<Map<String, String>> parseJson(MultipartFile file) throws Exception {
        List<Map<String, String>> raw = objectMapper.readValue(
                file.getInputStream(),
                new TypeReference<>() {}
        );
        // Normalize keys to lowercase
        List<Map<String, String>> rows = new ArrayList<>();
        for (Map<String, String> entry : raw) {
            Map<String, String> row = new LinkedHashMap<>();
            entry.forEach((k, v) -> row.put(k.toLowerCase(), v != null ? v : ""));
            rows.add(row);
        }
        return rows;
    }

    private String getOrDefault(Map<String, String> map, String key, String defaultVal) {
        String val = map.get(key);
        return val != null ? val : defaultVal;
    }
}
