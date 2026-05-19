package com.service.implement;

import com.DTO.ProductDTO;
import com.DTO.ProductVariantDTO;
import com.DTO.VariantStatsDTO;
import com.entity.*;
import com.exception.InvalidRequestException;
import com.exception.NotFoundObjectRequestException;
import com.mapper.ProductMapper;
import com.mapper.ProductVariantMapper;
import com.repository.*;
import com.request.CreateProductRequest;
import com.request.UpdateProductRequest;
import com.response.ProductStatsResponse;
import com.service.AI.ProductEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;
    private final ProductVariantMapper productVariantMapper;
    private final ProductEmbeddingService productEmbeddingService;

    public Page<ProductDTO> getAllProducts(int page, int size, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Product> products = productRepository.searchAllWithKeyWord(keyword, pageRequest);

        return products.map(productMapper::toDto);
    }

    public List<ProductDTO> getProductsByIds(List<Long> ids) {
        List<Product> products = productRepository.findAllById(ids);
        return productMapper.toDtos(products);
    }

    public List<ProductVariantDTO> getProductVariantsByIds(List<Long> ids) {
        List<ProductVariant> products = productVariantRepository.findAllById(ids);
        return productVariantMapper.toDtos(products);
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public ProductDTO getProductDTOById(Long id) {
        Product product = productRepository.findWithDetailById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return productMapper.toFullDto(product);
    }

    public ProductDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return productMapper.toFullDto(product);
    }

    public ProductStatsResponse getStats(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        LocalDate today = LocalDate.now();

        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime weekStart = today
                .with(WeekFields.of(Locale.forLanguageTag("vi")).dayOfWeek(), 1)
                .atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime yearStart = today.withDayOfYear(1).atStartOfDay();

        List<VariantStatsDTO> variantStats = orderItemRepository
                .getVariantStatsByProduct(productId, dayStart, weekStart, monthStart, yearStart);

        long soldToday = variantStats.stream().mapToLong(v -> v.getSoldToday() != null ? v.getSoldToday() : 0).sum();
        long soldThisWeek = variantStats.stream().mapToLong(v -> v.getSoldThisWeek() != null ? v.getSoldThisWeek() : 0)
                .sum();
        long soldThisMonth = variantStats.stream()
                .mapToLong(v -> v.getSoldThisMonth() != null ? v.getSoldThisMonth() : 0).sum();
        long soldThisYear = variantStats.stream().mapToLong(v -> v.getSoldThisYear() != null ? v.getSoldThisYear() : 0)
                .sum();
        double revenueToday = variantStats.stream()
                .mapToDouble(v -> v.getRevenueTotal() != null ? v.getRevenueTotal() : 0).sum();

        return ProductStatsResponse.builder()
                .productId(productId)
                .productName(product.getName())
                .totalViewCount(product.getViewCount())
                .totalSoldCount(product.getSoldCount())
                .soldToday(soldToday)
                .soldThisWeek(soldThisWeek)
                .soldThisMonth(soldThisMonth)
                .soldThisYear(soldThisYear)
                .revenueToday(revenueToday)
                .variantStats(variantStats)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByCategory(
            Long categoryId, int page, int size,
            List<Long> subCategoryIds,
            List<Long> brandIds,
            String sort,
            Long minPrice,
            Long maxPrice) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Category> categories = categoryRepository.findAllSubCategories(categoryId);

        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();

        if (subCategoryIds != null && !subCategoryIds.isEmpty()) {
            categoryIds = subCategoryIds;
        }

        if (minPrice == null) {
            minPrice = 0L;
        }
        if (maxPrice == null) {
            maxPrice = 9999999L;
        }
        System.out.println(minPrice + " " + maxPrice);
        Page<Product> products = productRepository.findWithFilter(
                categoryIds,
                brandIds,
                sort,
                minPrice,
                maxPrice, pageRequest);

        return products.map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByBrand(
            Long brandId, int page, int size,
            List<Long> subCategoryIds,
            String sort,
            Long minPrice,
            Long maxPrice) {
        PageRequest pageRequest = PageRequest.of(page, size);
        List<Category> categories = productRepository.findCategoriesByBrandId(brandId);

        List<Long> categoryIds = categories.stream()
                .map(Category::getId)
                .toList();

        if (subCategoryIds != null && !subCategoryIds.isEmpty()) {
            categoryIds = subCategoryIds;
        }

        if (minPrice == null) {
            minPrice = 0L;
        }
        if (maxPrice == null) {
            maxPrice = 9999999L;
        }
        List<Long> brandIds = new ArrayList<>();
        brandIds.add(brandId);
        Page<Product> products = productRepository.findWithFilter(
                categoryIds,
                brandIds,
                sort,
                minPrice,
                maxPrice, pageRequest);

        return products.map(productMapper::toDto);
    }



    @Transactional
    public Page<ProductDTO> searchProducts(String keyword, int page, int size, long minPrice,
            long maxPrice, List<Long> brandIds,
            List<Long> subCategoryIds, String sort, boolean inStock) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Product> products = productRepository.search(keyword, minPrice, maxPrice, brandIds,
                subCategoryIds, sort, inStock,
                pageRequest);
        return products.map(productMapper::toDto);
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundObjectRequestException("Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new NotFoundObjectRequestException("Brand not found"));
        Product newProduct = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .slug(request.getSlug())
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .price(request.getPrice())
                .salePrice(request.getSalePrice())
                .stockQuantity(request.getStockQuantity())
                .mainImage(request.getMainImage())
                .category(category)
                .brand(brand)
                .build();

        if (request.getProductVariants() != null && !request.getProductVariants().isEmpty()) {
            List<ProductVariantDTO> va = request.getProductVariants();
            List<ProductVariant> productVariants = new ArrayList<>();
            for (ProductVariantDTO i : va) {
                ProductVariant productVariant = ProductVariant.builder()
                        .product(newProduct)
                        .name(i.getName())
                        .sku(i.getSku())
                        .price(i.getPrice())
                        .salePrice(i.getSalePrice())
                        .stockQuantity(i.getStockQuantity())
                        .mainImage(i.getMainImage())
                        .attributes(i.getAttributes())
                        .build();
                productVariants.add(productVariant);
            }

            newProduct.setProductVariants(productVariants);
        }
        newProduct = productRepository.save(newProduct);

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            Set<ProductImage> images = new HashSet<>(imageRepository.findAllById(request.getImageIds()));
            if (images.size() != request.getImageIds().size()) {
                throw new InvalidRequestException("Some images not found");
            }
            for (ProductImage i : images) {
                i.setProduct(newProduct);
            }
            imageRepository.saveAll(images);
            newProduct.setImages(images);
        }

        productEmbeddingService.embedAndSave(newProduct);

        return productMapper.toFullDto(newProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, UpdateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundObjectRequestException("Category not found"));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new NotFoundObjectRequestException("Brand not found"));

        Product product = productRepository.findWithDetailById(id).orElseThrow(
                () -> new NotFoundObjectRequestException("Không tìm thấy sản phẩm!"));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setSlug(request.getSlug());
        product.setShortDescription(request.getShortDescription());
        product.setFullDescription(request.getFullDescription());
        product.setPrice(request.getPrice());
        product.setSalePrice(request.getSalePrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMainImage(request.getMainImage());
        product.setCategory(category);
        product.setBrand(brand);

        product.getProductVariants().clear();
        if (request.getProductVariants() != null) {
            List<ProductVariantDTO> va = request.getProductVariants();
            for (ProductVariantDTO i : va) {
                ProductVariant productVariant = ProductVariant.builder()
                        .product(product)
                        .name(i.getName())
                        .sku(i.getSku())
                        .price(i.getPrice())
                        .salePrice(i.getSalePrice())
                        .stockQuantity(i.getStockQuantity())
                        .mainImage(i.getMainImage())
                        .attributes(i.getAttributes())
                        .build();
                product.getProductVariants().add(productVariant);
            }
        }
        // ===== update images =====
        product.getImages().forEach(img -> img.setProduct(null));
        product.getImages().clear();

        if (request.getImageIds() != null) {
            Set<ProductImage> images = new HashSet<>(imageRepository.findAllById(request.getImageIds()));

            for (ProductImage img : images) {
                img.setProduct(product);
            }
            product.setImages(images);
        }

        return productMapper.toFullDto(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional
    public void increaseViewCount(Long productId) {
        Product product = findProductById(productId);
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }

    public List<ProductDTO> getNewProducts(Long categoryId) {
        int limit = 10;
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<Product> products = new ArrayList<>();
        if (categoryId == null) {
            products = productRepository.newProduct((Pageable) pageRequest);
        } else {
            products = productRepository.newProductByCategory((Pageable) pageRequest, categoryId);
        }

        return productMapper.toFullDtos(products);
    }
}