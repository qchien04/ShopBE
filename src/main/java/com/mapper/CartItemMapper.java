package com.mapper;

import com.DTO.CartItemDTO;
import com.entity.CartItem;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring",uses = {
        ProductMapper.class,
})
public interface CartItemMapper {

    @Named("toDto")
    @Mapping(target = "cartId", source = "cart.id")
    @Mapping(
            target = "productVariant",
            ignore = true
    )
    CartItemDTO toDto(CartItem cartItem);

    @IterableMapping(qualifiedByName = "toDto")
    List<CartItemDTO> toDtos(List<CartItem> cartItems);


    @Named("toFullDto")
    @Mapping(target = "cartId", source = "cart.id")
    CartItemDTO toFullDto(CartItem cartItem);

    @IterableMapping(qualifiedByName = "toFullDto")
    List<CartItemDTO> toFullDtos(List<CartItem> cartItems);
}
