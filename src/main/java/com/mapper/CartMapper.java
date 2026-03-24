package com.mapper;

import com.DTO.CartDTO;
import com.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring",uses = {
        CartItemMapper.class,
})
public interface CartMapper {
    @Mapping(
            target = "items",
            qualifiedByName = "toFullDto"
    )
    CartDTO toDto(Cart cart);

}
