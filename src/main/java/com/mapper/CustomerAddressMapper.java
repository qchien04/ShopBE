package com.mapper;

import com.DTO.CustomerAddressDTO;
import com.entity.CustomerAddress;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerAddressMapper {

    CustomerAddressDTO toDto(CustomerAddress r);
    
    // Force recompile to generate mappings for new GHN fields
    List<CustomerAddressDTO> toDtos(List<CustomerAddress> rs);
}
