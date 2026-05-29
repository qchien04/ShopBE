package com.service.implement;
import com.DTO.CategoryDTO;
import com.entity.*;
import com.exception.InvalidRequestException;
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

    public CategoryDTO getCategoryById(Long id) {
        Category category= categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return categoryMapper.toDto(category);
    }

    public CategoryDTO getCategoryBySlug(String slug) {
        Category category= categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return categoryMapper.toDto(category);
    }

    @Transactional
    public CategoryDTO createCategory(CreateCategoryRequest r) {
        if (categoryRepository.findBySlug(r.getSlug()).isPresent()) {
            throw new InvalidRequestException("Slug danh mục đã tồn tại!");
        }
        Category category= Category.builder()
                .name(r.getName())
                .description(r.getDescription())
                .slug(r.getSlug())
                .image(r.getImage())
                .build();

        if (r.getParentId() != null) {
            Category parent = categoryRepository.findById(r.getParentId())
                    .orElseThrow(()->new NotFoundObjectRequestException("Không tìm thấy danh mục cha!"));;
            category.setParent(parent);
        }

        Category returnCategory= categoryRepository.save(category);

        return categoryMapper.toDto(returnCategory);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(()->new NotFoundObjectRequestException("Không tìm thấy sản phẩm!"));
        categoryRepository.findBySlug(request.getSlug()).ifPresent(existingCategory -> {
            if (!existingCategory.getId().equals(id)) {
                throw new InvalidRequestException("Slug danh mục đã tồn tại!");
            }
        });
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImage(request.getImage());
        category.setSlug(request.getSlug());

        if (request.getParentId() != null) {
            if (request.getParentId().equals(id)) {
                throw new RuntimeException("Category cannot be its own parent");
            }
            Category parent = categoryRepository.findById(request.getParentId()).orElseThrow(()->new NotFoundObjectRequestException("Không tìm thấy sản phẩm!"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category returnCategory= categoryRepository.save(category);
        return categoryMapper.toDto(returnCategory);
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