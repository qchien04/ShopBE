package com.mapper;
import com.DTO.ProductDTO;
import com.entity.Product;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {
            ProductImageMapper.class,
            ProductVariantMapper.class,
            CategoryMapper.class
        })
public interface ProductMapper {
    @Named("toDto")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "productVariants", ignore = true)
    @Mapping(target = "category", qualifiedByName = "toDto")
    ProductDTO toDto(Product r);

    @IterableMapping(qualifiedByName = "toDto")
    List<ProductDTO> toDtos(List<Product> rs);


    @Named("toFullDto")
    @Mapping(target = "category", qualifiedByName = "toDto")
    ProductDTO toFullDto(Product r);

    @IterableMapping(qualifiedByName = "toFullDto")
    List<ProductDTO> toFullDtos(List<Product> rs);




}
