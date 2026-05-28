package com.mapper;

import com.DTO.OrderItemDTO;
import com.entity.OrderItem;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring",uses = {
        ProductMapper.class,
})
public interface OrderItemMapper {

    @Named("toDto")
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "productVariantId", source = "productVariant.id")
    @Mapping(target = "productId", expression = "java(mapProductId(orderItem))")
    OrderItemDTO toDto(OrderItem orderItem);

    default Long mapProductId(OrderItem orderItem) {
        if (orderItem.getProduct() != null) {
            return orderItem.getProduct().getId();
        }
        if (orderItem.getProductVariant() != null && orderItem.getProductVariant().getProduct() != null) {
            return orderItem.getProductVariant().getProduct().getId();
        }
        return null;
    }

    @IterableMapping(qualifiedByName = "toDto")
    List<OrderItemDTO> toDtos(List<OrderItem> items);

}
