package com.repository.Ai;

import com.DTO.ProductEmbeddingProjection;
import com.entity.AI.ProductEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductEmbeddingRepository
        extends JpaRepository<ProductEmbedding, Long> {

    @Query(value = """
        SELECT product_id as productId, content
        FROM product_embeddings
        ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<ProductEmbeddingProjection> searchSimilar(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit
    );

    @Query(value = """
        SELECT product_id as productId, content
        FROM product_embeddings
        WHERE product_id != :productId
        ORDER BY embedding <=> (SELECT embedding FROM product_embeddings WHERE product_id = :productId)
        LIMIT :limit
        """, nativeQuery = true)
    List<ProductEmbeddingProjection> findSimilarProducts(
            @Param("productId") Long productId,
            @Param("limit") int limit
    );
}