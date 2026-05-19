package com.request;

import com.DTO.ProductVariantDTO;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProductRequest {
    private Long id;
    private String name;
    private String sku;
    private String slug;
    private String shortDescription;
    private String fullDescription;
    private Double price;
    private Double salePrice;
    private Integer stockQuantity = 0;
    private String mainImage;
    private Long categoryId;
    private Long brandId;
    private List<Long> imageIds;
    private List<ProductVariantDTO> productVariants;
}

