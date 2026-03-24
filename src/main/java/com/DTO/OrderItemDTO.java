package com.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class OrderItemDTO {
    private Long id;
    private Long orderId;
    private Long productVariantId;
    private String productName;
    private String productSku;
    private Map<String, String> attributes;
    private String productImage;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}
