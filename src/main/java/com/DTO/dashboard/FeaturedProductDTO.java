package com.DTO.dashboard;

import com.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─── Sản phẩm nổi bật ───────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturedProductDTO {
    private Long id;
    private String name;

    private String mainImage;
    private String brand;
    private String category;
    private Double price;
    private Double salePrice;
    private Integer stockQuantity;
    private Integer soldCount;

}
