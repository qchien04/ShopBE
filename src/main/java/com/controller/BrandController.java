package com.controller;

import com.entity.Brand;
import com.request.CreateBrandRequest;
import com.request.UpdateBrandRequest;
import com.response.BrandFilterResponse;
import com.service.implement.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<Brand>> getAllCategories() {
        return ResponseEntity.ok(brandService.getAllBrand());
    }

    @GetMapping("/filter")
    public ResponseEntity<BrandFilterResponse> getPro(@RequestParam Long brandId) {
        return ResponseEntity.ok(brandService.getBrandFilter(brandId));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Brand> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Brand> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(brandService.getBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<Brand> createCategory(@RequestBody CreateBrandRequest request) {
        return ResponseEntity.ok(brandService.createBrand(request));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Brand> updateCategory(@PathVariable Long id,
                                                   @RequestBody UpdateBrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
