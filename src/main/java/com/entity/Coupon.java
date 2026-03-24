package com.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Double discountValue;
    private Double minOrderValue;
    private Double maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount = 0;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DiscountType { PERCENTAGE, FIXED_AMOUNT }

}

