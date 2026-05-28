package com.service.implement;

import com.entity.Brand;
import com.entity.Category;
import com.exception.InvalidRequestException;
import com.exception.NotFoundObjectRequestException;
import com.mapper.CartMapper;
import com.mapper.CategoryMapper;
import com.repository.BrandRepository;
import com.repository.CategoryRepository;
import com.repository.ProductRepository;
import com.request.CreateBrandRequest;
import com.request.CreateCategoryRequest;
import com.request.UpdateBrandRequest;
import com.request.UpdateCategoryRequest;
import com.response.BrandFilterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public BrandFilterResponse getBrandFilter(Long brandId) {

        List<Category> categories =
                productRepository.findCategoriesByBrandId(brandId);

        List<Object[]> results =
                productRepository.findMinAndMaxPriceByBrand(brandId);

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

        return new BrandFilterResponse(
                minPrice,
                maxPrice,
                categoryMapper.toDtos(categories)
        );
    }

    public List<Brand> getAllBrand() {
        return brandRepository.findAll();
    }

    public Brand getById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
    }

    public Brand getBySlug(String slug) {
        return brandRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
    }

    @Transactional
    public Brand createBrand(CreateBrandRequest r) {
        if (brandRepository.findBySlug(r.getSlug()).isPresent()) {
            throw new InvalidRequestException("Slug thương hiệu đã tồn tại!");
        }
        Brand brand=Brand.builder()
                .name(r.getName())
                .slug(r.getSlug())
                .description(r.getDescription())
                .logo(r.getLogo())
                .website(r.getWebsite())
                .build();
        return brandRepository.save(brand);
    }

    @Transactional
    public Brand updateBrand(Long id, UpdateBrandRequest request) {
        Brand brand = brandRepository.findById(id).orElseThrow(()->new NotFoundObjectRequestException("Brand not found!"));
        brandRepository.findBySlug(request.getSlug()).ifPresent(existingBrand -> {
            if (!existingBrand.getId().equals(id)) {
                throw new InvalidRequestException("Slug thương hiệu đã tồn tại!");
            }
        });
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setLogo(request.getLogo());
        brand.setWebsite(request.getWebsite());
        brand.setSlug(request.getSlug());
        return brandRepository.save(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }
}