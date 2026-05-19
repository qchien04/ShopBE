package com.repository;

import com.entity.Category;
import com.entity.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    @Query("SELECT c FROM Category c WHERE c.parent.id is NULL")
    @EntityGraph(attributePaths = {"children"})
    List<Category> findAllParent();

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    List<Category> findChildrenByParentId(Long parentId);

    @Query(value = """
        WITH RECURSIVE subcategories AS (
            SELECT * FROM categories WHERE id = :categoryId
            UNION ALL
            SELECT c.* FROM categories c
            INNER JOIN subcategories sc ON c.parent_id = sc.id
        )
        SELECT * FROM subcategories
    """, nativeQuery = true)
    List<Category> findAllSubCategories(Long categoryId);



}