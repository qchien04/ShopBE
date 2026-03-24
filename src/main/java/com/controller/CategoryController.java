package com.controller;

import com.DTO.CategoryDTO;
import com.entity.Category;
import com.request.CreateCategoryRequest;
import com.request.UpdateCategoryRequest;
import com.response.CategoryFilterResponse;
import com.service.implement.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/parent")
    public ResponseEntity<List<CategoryDTO>> getParentCategory() {
        return ResponseEntity.ok(categoryService.getAllParentCategories());
    }

    @GetMapping("/filter")
    public ResponseEntity<CategoryFilterResponse> getPro(@RequestParam Long categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryFilter(categoryId));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id,
                                                   @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
