package com.repository;

import com.entity.Brand;
import com.entity.Category;
import com.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = { "images", "productVariants", "category", "brand" })
    Optional<Product> findBySlug(String slug);



    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByBrandId(Long brandId);

    @Query("""
                SELECT p FROM Product p
                WHERE
                    (:keyword IS NULL OR
                        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                AND (:minPrice IS NULL OR :maxPrice IS NULL
                     OR p.salePrice BETWEEN :minPrice AND :maxPrice)
                AND (:brandIds IS NULL OR p.brand.id IN :brandIds)
                AND (:subCategoryIds IS NULL OR p.category.id IN :subCategoryIds)
                AND (:inStock IS NULL OR (:inStock = true AND p.stockQuantity > 0) OR (:inStock = false AND p.stockQuantity <= 0))
            """)
    @EntityGraph(attributePaths = { "category", "brand" })
    Page<Product> search(
            @Param("keyword") String keyword,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            @Param("brandIds") List<Long> brandIds,
            @Param("subCategoryIds") List<Long> subCategoryIds,
            @Param("inStock") Boolean inStock,
            Pageable pageable);

    @Query("""
                SELECT p FROM Product p
                WHERE
                    (:keyword IS NULL OR
                        LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                        LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
            """)
    @EntityGraph(attributePaths = { "category", "brand" })
    Page<Product> searchAllWithKeyWord(
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
                SELECT p FROM Product p
                WHERE p.category.id IN :categoryIds
                  AND (:brandIds IS NULL OR p.brand.id IN :brandIds)
                  AND p.salePrice BETWEEN :minPrice AND :maxPrice
            """)
    Page<Product> findWithFilter(
            List<Long> categoryIds,
            List<Long> brandIds,
            Long minPrice,
            Long maxPrice,
            Pageable pageRequest);

    @Query("SELECT p FROM Product p WHERE p.salePrice IS NOT NULL AND p.salePrice < p.price")
    List<Product> findProductsOnSale();

    @Query("""
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.images
        LEFT JOIN FETCH p.productVariants
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.brand
        WHERE p.id = :id
    """)
    Optional<Product> findWithDetailById(Long id);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    @EntityGraph(attributePaths = { "images", "productVariants", "category", "brand" })
    List<Product> newProduct(Pageable pageable);

    @Query("SELECT p FROM Product p where p.category.id=:categoryId ORDER BY p.createdAt DESC")
    @EntityGraph(attributePaths = { "images", "productVariants", "category", "brand" })
    List<Product> newProductByCategory(Pageable pageable, @Param("categoryId") Long categoryId);

    @Query("""
                SELECT DISTINCT p.brand
                FROM Product p
                WHERE p.category.id IN :categoryIds
                  AND p.brand IS NOT NULL
            """)
    List<Brand> findBrandsByCategoryIds(List<Long> categoryIds);

    @Query("""
                SELECT MIN(p.salePrice), MAX(p.salePrice)
                FROM Product p
                WHERE p.category.id IN :categoryIds
            """)
    List<Object[]> findMinAndMaxPrice(List<Long> categoryIds);

    @Query("""
                SELECT p
                FROM Product p
                WHERE p.category.id IN :categoryIds
            """)
    List<Product> findByCategoryIds(List<Long> categoryIds);

    @Query("""
                SELECT MIN(COALESCE(p.salePrice, p.price)),
                       MAX(COALESCE(p.salePrice, p.price))
                FROM Product p
                WHERE p.brand.id = :brandId
            """)
    List<Object[]> findMinAndMaxPriceByBrand(Long brandId);

    // Top bán chạy
    @Query("SELECT p FROM Product p ORDER BY p.soldCount DESC")
    List<Product> findTopBySoldCount(Pageable pageable);

    // Sản phẩm nổi bật (featured = true hoặc soldCount cao)
    @Query("SELECT p FROM Product p ORDER BY p.soldCount DESC")
    List<Product> findFeatured(Pageable pageable);

    // Đếm sắp hết hàng
    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity > 0 AND p.stockQuantity < :threshold")
    Long countLowStock(@Param("threshold") int threshold);

    @Query("""
                SELECT DISTINCT p.category
                FROM Product p
                WHERE p.brand.id = :brandId
                  AND p.category IS NOT NULL
            """)
    List<Category> findCategoriesByBrandId(@Param("brandId") Long brandId);
}