package com.repository;

import com.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /** Tất cả promotion đang hoạt động tại thời điểm hiện tại */
    @Query("""
        SELECT p FROM Promotion p
        WHERE p.active = true
          AND p.startDate <= :now
          AND p.endDate >= :now
          AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)
        ORDER BY p.priority DESC NULLS LAST
    """)
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);

    /** Promotion theo loại, đang hoạt động */
    @Query("""
        SELECT p FROM Promotion p
        WHERE p.active = true
          AND p.type = :type
          AND p.startDate <= :now
          AND p.endDate >= :now
          AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)
        ORDER BY p.priority DESC NULLS LAST
    """)
    List<Promotion> findActiveByType(@Param("type") Promotion.PromotionType type,
                                     @Param("now") LocalDateTime now);

    /** Flash Sale đang hoạt động */
    @Query("""
        SELECT p FROM Promotion p
        WHERE p.active = true
          AND p.type = 'FLASH_SALE'
          AND p.startDate <= :now
          AND p.endDate >= :now
        ORDER BY p.priority DESC NULLS LAST
    """)
    List<Promotion> findActiveFlashSales(@Param("now") LocalDateTime now);

    /** Tất cả (bao gồm inactive) - cho admin */
    List<Promotion> findAllByOrderByCreatedAtDesc();

    Optional<Promotion> findById(Long id);
}
