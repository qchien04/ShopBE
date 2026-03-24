package com.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "banners")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String imageUrl;
    private String link;

    @Enumerated(EnumType.STRING)
    private BannerPosition position;

    private Integer displayOrder = 0;
    private Boolean active = true;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BannerPosition {
        HOME_MAIN, HOME_SIDE, CATEGORY_TOP, PRODUCT_DETAIL
    }
}
