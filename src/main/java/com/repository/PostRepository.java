package com.repository;

import com.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByStatus(Post.PostStatus status, Pageable pageable);

    Page<Post> findAll(Pageable pageable);

    Page<Post> findByCategory(String category, Pageable pageable);

    Page<Post> findByStatusAndCategory(Post.PostStatus status, String category, Pageable pageable);

    @Query(value = "SELECT * FROM post ORDER BY RAND() LIMIT 4", nativeQuery = true)
    List<Post> findRandomPosts();

    @Query("""
                SELECT p FROM Post p
                WHERE (:status IS NULL OR p.status = :status)
                  AND (:category IS NULL OR p.category = :category)
                  AND (:keyword IS NULL OR
                       LOWER(p.title) LIKE :keyword
                       OR LOWER(p.description) LIKE :keyword)
            """)
    @EntityGraph(attributePaths = { "tags" })
    Page<Post> search(
            @Param("status") Post.PostStatus status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);
}
