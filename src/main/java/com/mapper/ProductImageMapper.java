package com.mapper;

import com.DTO.ProductImageDTO;
import com.entity.ProductImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImageDTO toDto(ProductImage r);
    List<ProductImageDTO> toDtos(List<ProductImage> rs);
}
