package com.DTO;

import lombok.Data;

import java.util.Map;

@Data
public class ProductVariantDTO {
    private Long id;
    private Long productId;
    private String name;
    private String sku;
    private Double price;
    private Double salePrice;
    private Integer stockQuantity;
    private String mainImage;
    private Map<String, String> attributes;
}
