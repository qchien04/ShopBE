package com.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Table(name = "promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;

    // ── Thời gian hiệu lực ─────────────────────────────────────────────
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    // ── Trạng thái ─────────────────────────────────────────────────────
    @Builder.Default
    private Boolean active = true;

    private Integer priority;          // Ưu tiên khi nhiều promotion cùng áp dụng

    // ── Giới hạn sử dụng ───────────────────────────────────────────────
    private Integer usageLimit;        // null = không giới hạn
    @Builder.Default
    private Integer usedCount = 0;

    private Integer perUserLimit;      // null = không giới hạn per user

    // ── Flash Sale ─────────────────────────────────────────────────────
    // Lưu danh sách productId + giá flash sale (dạng JSON)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<FlashSaleItem> flashSaleItems;

    // ── Discount theo bậc đơn hàng (Tier) ─────────────────────────────
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<OrderTier> orderTiers;

    // ── Áp dụng cho category / brand ───────────────────────────────────
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Long> applyCategoryIds;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private List<Long> applyBrandIds;

    private Double discountValue;
    @Enumerated(EnumType.STRING)
    private Coupon.DiscountType discountType; // PERCENTAGE | FIXED_AMOUNT

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // ── Enums ───────────────────────────────────────────────────────────
    public enum PromotionType {
        FLASH_SALE,        // Flash sale giảm giá sản phẩm trong khoảng thời gian
        ORDER_TIER,        // Giảm giá theo bậc đơn hàng
        CATEGORY_DISCOUNT, // Giảm giá toàn bộ danh mục
        BRAND_DISCOUNT,    // Giảm giá toàn bộ thương hiệu
        FREE_SHIPPING      // Miễn phí vận chuyển
    }

    // ── Embedded Value Objects (stored as JSON) ──────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class FlashSaleItem {
        private Long productId;
        private Long variantId;
        private Double discountPercent;
        private Double fixedPrice;
        private Integer stockLimit;
        private Integer soldCount = 0;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class OrderTier {
        private Double minOrderValue;   // Giá trị đơn tối thiểu
        private Double discountValue;   // Số tiền / % giảm
        private Coupon.DiscountType discountType;
        private Double maxDiscountAmount; // Giới hạn giảm tối đa (cho PERCENTAGE)
    }
}
