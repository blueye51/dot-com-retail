package com.eric.store.brands.controller;

import com.eric.store.brands.dto.BrandResponse;
import com.eric.store.brands.entity.Brand;
import com.eric.store.products.mapper.ProductMapper;
import com.eric.store.brands.service.BrandService;
import jakarta.validation.Valid;
import com.eric.store.brands.dto.BrandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        List<BrandResponse> brands = brandService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UUID> createBrand(@RequestBody @Valid BrandRequest req) {
        Brand brand = brandService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(brand.getId());
    }
}
