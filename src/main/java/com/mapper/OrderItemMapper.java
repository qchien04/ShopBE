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
    OrderItemDTO toDto(OrderItem orderItem);

    @IterableMapping(qualifiedByName = "toDto")
    List<OrderItemDTO> toDtos(List<OrderItem> items);

}
