package com.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String icon;
    private String description;
    private String slug;
    private String image;
    private Boolean active;
    private Long parentId;
    private List<CategoryDTO> children;
}

