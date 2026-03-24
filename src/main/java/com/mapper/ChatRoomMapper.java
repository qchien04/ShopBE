package com.mapper;

import com.DTO.CartItemDTO;
import com.DTO.ChatRoomDTO;
import com.entity.CartItem;
import com.entity.ChatRoom;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatRoomMapper {
    @Named("toSmallllDto")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", ignore = true)
    @Mapping(target = "customerAvt", ignore = true)
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "staffName", ignore = true)
    ChatRoomDTO toSmallDto(ChatRoom cartItem);

    @IterableMapping(qualifiedByName = "toSmallllDto")
    List<ChatRoomDTO> toSmallDtos(List<ChatRoom> cartItems);


    @Named("toDto")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.fullName")
    @Mapping(target = "customerAvt", source = "customer.avt")
    @Mapping(target = "staffId", source = "staff.id")
    @Mapping(target = "staffName", ignore = true)
    ChatRoomDTO toDto(ChatRoom cartItem);

    @IterableMapping(qualifiedByName = "toDto")
    List<ChatRoomDTO> toDtos(List<ChatRoom> cartItems);
}
