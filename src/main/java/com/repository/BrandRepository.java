package com.repository;

import com.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByName(String name);
    Optional<Brand> findBySlug(String slug);
    List<Brand> findByActiveTrue();

    @Query("""
        SELECT DISTINCT p.brand
        FROM Product p
        WHERE p.category.id = :categoryId
          AND p.brand IS NOT NULL
    """)
    List<Brand> findBrandsByCategoryId(Long categoryId);
}
