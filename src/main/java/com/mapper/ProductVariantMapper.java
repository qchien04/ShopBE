package com.mapper;

import com.DTO.ProductVariantDTO;
import com.DTO.SpecificationDTO;
import com.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    @Mapping(target = "productId", source = "product.id")
    ProductVariantDTO toDto(ProductVariant r);

    List<ProductVariantDTO> toDtos(List<ProductVariant> rs);
}
