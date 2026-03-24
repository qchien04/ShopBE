package com.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WishlistDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String mainImage;
    private Double price;
    private Double salePrice;
    private String status;
    private LocalDateTime addedAt;
}
