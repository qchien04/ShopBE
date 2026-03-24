package com.service.implement;
import com.DTO.CategoryDTO;
import com.entity.*;
import com.exception.NotFoundObjectRequestException;
import com.mapper.CategoryMapper;
import com.repository.*;
import com.request.CreateCategoryRequest;
import com.request.UpdateCategoryRequest;
import com.response.CategoryFilterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories=categoryRepository.findAll();
        return categoryMapper.toDtos(categories);
    }

    @Transactional
    public List<CategoryDTO> getAllParentCategories() {
        List<Category> categories=categoryRepository.findAllParent();
        return categoryMapper.toFullDtos(categories);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest r) {
        Category category= Category.builder()
                .name(r.getName())
                .description(r.getDescription())
                .slug(r.getSlug())
                .image(r.getImage())
                .icon(r.getIcon())
                .active(true)
                .build();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = getCategoryById(id);
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());
        category.setSlug(request.getSlug());
        category.setIcon(request.getIcon());
        category.setActive(request.getActive());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundObjectRequestException("Category not found"));

        List<Product> products = productRepository.findByCategoryId(id);

        for (Product product : products) {
            product.setCategory(null);
        }

        productRepository.saveAll(products);
        categoryRepository.delete(category);
    }

    @Transactional
    public CategoryFilterResponse getCategoryFilter(Long categoryId) {

        List<Category> subCategories = categoryRepository.findChildrenByParentId(categoryId);

        List<Long> categoryIds = new java.util.ArrayList<>(subCategories.stream()
                .map(Category::getId)
                .toList());

        categoryIds.add(categoryId);

        System.out.println(categoryIds.getFirst());

        List<Brand> brands = productRepository.findBrandsByCategoryIds(categoryIds);

        List<Object[]> results = productRepository.findMinAndMaxPrice(categoryIds);

        Long minPrice = 0L;
        Long maxPrice = 0L;

        if (!results.isEmpty()) {
            Object[] row = results.get(0);

            if (row[0] != null) {
                minPrice = ((Number) row[0]).longValue();
            }

            if (row[1] != null) {
                maxPrice = ((Number) row[1]).longValue();
            }
        }

        return new CategoryFilterResponse(minPrice,maxPrice, categoryMapper.toDtos(subCategories), brands);
    }
}