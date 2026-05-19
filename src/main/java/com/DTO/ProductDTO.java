package com.DTO;
import com.entity.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ProductDTO {
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
    private CategoryDTO category;
    private Brand brand;

    private Integer viewCount = 0;
    private Integer soldCount = 0;
    private Set<ProductImageDTO> images;
    private Set<ProductVariantDTO> productVariants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
