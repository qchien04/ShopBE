package com.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productVariantId;
    private Long productId;
    private String productName;
    private String productSlug;
    private Map<String, String> attributes;
    private String productImage;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}
