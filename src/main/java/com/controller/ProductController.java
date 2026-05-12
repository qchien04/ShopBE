package com.controller;

import com.DTO.ProductDTO;
import com.DTO.ProductVariantDTO;
import com.entity.Product;
import com.request.CreateProductRequest;
import com.request.UpdateProductRequest;
import com.response.PageResponse;
import com.response.ProductStatsResponse;
import com.service.implement.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PageResponse<ProductDTO>> getAllProducts(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String keyword) {
        if (ids == null) {
            Page<ProductDTO> dtoPage = productService.getAllProducts(page, size, keyword);
            PageResponse<ProductDTO> response = new PageResponse<>(
                    dtoPage.getContent(),
                    dtoPage.getNumber(),
                    dtoPage.getSize(),
                    dtoPage.getTotalElements(),
                    dtoPage.getTotalPages());

            return ResponseEntity.ok(response);
        } else {
            List<ProductDTO> productDTOS = productService.getProductsByIds(ids);
            PageResponse<ProductDTO> response = new PageResponse<>(
                    productDTOS,
                    0,
                    productDTOS.size(),
                    productDTOS.size(),
                    1);
            return ResponseEntity.ok(response);
        }

    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ProductStatsResponse> getProductStats(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getStats(id));
    }

    @GetMapping("/variant")
    public ResponseEntity<List<ProductVariantDTO>> getVariantAllProducts(
            @RequestParam(required = true) List<Long> ids) {
        return ResponseEntity.ok(productService.getProductVariantsByIds(ids));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDTOById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<PageResponse<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(required = false) List<Long> subCategoryIds,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "0") Long minPrice,
            @RequestParam(defaultValue = "9999999999") Long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Page<ProductDTO> dtoPage = productService.getProductsByCategory(categoryId, page, size, subCategoryIds,
                brandIds, sort, minPrice, maxPrice);
        PageResponse<ProductDTO> response = new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<PageResponse<ProductDTO>> getProductsByBrand(
            @PathVariable Long brandId,
            @RequestParam(required = false) List<Long> subCategoryIds,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "0") Long minPrice,
            @RequestParam(defaultValue = "9999999999") Long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        Page<ProductDTO> dtoPage = productService.getProductsByBrand(brandId, page, size, subCategoryIds, sort,
                minPrice, maxPrice);
        PageResponse<ProductDTO> response = new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/new")
    public ResponseEntity<List<ProductDTO>> getNewProducts(@RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(productService.getNewProducts(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) List<Long> subCategoryIds,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "0") Long minPrice,
            @RequestParam(defaultValue = "9999999999") Long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "true") boolean inStock) {
        Page<ProductDTO> dtoPage = productService.searchProducts(keyword, page, size, minPrice, maxPrice, brandIds,
                subCategoryIds, sort, inStock);
        PageResponse<ProductDTO> response = new PageResponse<>(
                dtoPage.getContent(),
                dtoPage.getNumber(),
                dtoPage.getSize(),
                dtoPage.getTotalElements(),
                dtoPage.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id,
            @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
        productService.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
}