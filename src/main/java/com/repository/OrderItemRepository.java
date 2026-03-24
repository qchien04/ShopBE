package com.repository;
import com.DTO.VariantStatsDTO;
import com.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("""
        SELECT new com.DTO.VariantStatsDTO(
            oi.productVariant.id,

            SUM(CASE WHEN o.createdAt >= :dayStart  THEN oi.quantity ELSE 0 END),
            SUM(CASE WHEN o.createdAt >= :weekStart THEN oi.quantity ELSE 0 END),
            SUM(CASE WHEN o.createdAt >= :monthStart THEN oi.quantity ELSE 0 END),
            SUM(CASE WHEN o.createdAt >= :yearStart  THEN oi.quantity ELSE 0 END),
            SUM(oi.quantity),

            SUM(CASE WHEN o.createdAt >= :dayStart  THEN oi.subtotal ELSE 0 END)
        )
        FROM OrderItem oi
        JOIN oi.order o
        WHERE oi.productVariant.product.id = :productId
          AND o.status NOT IN ('CANCELLED','RETURNED')
        GROUP BY oi.productVariant.id
    """)
    List<VariantStatsDTO> getVariantStatsByProduct(
            @Param("productId")   Long          productId,
            @Param("dayStart")    LocalDateTime dayStart,
            @Param("weekStart")   LocalDateTime weekStart,
            @Param("monthStart")  LocalDateTime monthStart,
            @Param("yearStart")   LocalDateTime yearStart
    );
}
