package com.DTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItemDTO {
    private Long id;
    private Long cartId;
    private ProductVariantDTO productVariant;
    private Integer quantity;
    private Double price;
    private LocalDateTime addedAt;
}
