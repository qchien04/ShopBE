package com.request;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String name;
    private String description;
    private String slug;
    private String icon;
    private String image;

    private Long parentId;
}