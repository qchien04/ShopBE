package com.response;

import com.DTO.CategoryDTO;
import com.entity.Brand;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BrandFilterResponse {
    private Long minPrice;
    private Long maxPrice;
    private List<CategoryDTO> subCategories;
}
