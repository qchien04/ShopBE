package com.mapper;

import com.DTO.CategoryDTO;
import com.entity.Category;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Named("toDto")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", ignore = true)
    CategoryDTO toDto(Category category);

    @IterableMapping(qualifiedByName = "toDto")
    List<CategoryDTO> toDtos(List<Category> categories);

    @Named("toFullDto")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", qualifiedByName = "toFullDto")
    CategoryDTO toFullDto(Category category);

    @IterableMapping(qualifiedByName = "toFullDto")
    List<CategoryDTO> toFullDtos(List<Category> categories);
}
