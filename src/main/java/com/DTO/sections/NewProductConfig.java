package com.DTO.sections;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewProductConfig {
    private String id;
    private String title;
    private boolean active;
    private List<CategoryWithProducts> categoryOfProduct;
    private Integer productPerRow;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryWithProducts {
        private List<Long> productIds;
        private Long categoryId;
    }
}
