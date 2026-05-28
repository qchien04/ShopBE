package com.request;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String name;
    private String description;
    private String slug;
    private String image;

    private Long parentId;
}