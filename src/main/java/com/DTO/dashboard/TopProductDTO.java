package com.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─── Top sản phẩm bán chạy ──────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private Long id;
    private String name;

    private String mainImage;
    private String brand;
    private String category;
    private Integer soldCount;
    private Double price;
    private Double salePrice;
}
