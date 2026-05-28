package com.mapper;

import com.DTO.OrderDTO;
import com.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;


@Mapper(componentModel = "spring",uses = {
        OrderItemMapper.class,
})
// Force rebuild for actualShippingFee
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "items", qualifiedByName = "toDto")
    OrderDTO toDto(Order order);

    List<OrderDTO> toDtos(List<Order> rs);

}
