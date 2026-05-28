package com.service.implement;

import com.DTO.CustomerAddressDTO;
import com.entity.*;
import com.exception.NotFoundObjectRequestException;
import com.mapper.CustomerAddressMapper;
import com.repository.CustomerAddressRepository;
import com.request.CreateCustomerAddressRequest;
import com.request.UpdateCustomerAddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerAddressMapper customerAddressMapper;

    public List<CustomerAddressDTO> getUserAddressDTO() {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return customerAddressMapper.toDtos(customerAddressRepository.findByUserId(myId));
    }

    @Transactional
    public CustomerAddressDTO createCustomerAddress(CreateCustomerAddressRequest r) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        CustomerAddress customerAddress=CustomerAddress.builder()
                .user(User.builder().id(myId).build())
                .fullName(r.getFullName())
                .phone(r.getPhone())
                .detailAddress(r.getDetailAddress())
                .lat(r.getLat())
                .lng(r.getLng())
                .isDefault(r.getIsDefault())
                .ghnProvinceId(r.getGhnProvinceId())
                .ghnDistrictId(r.getGhnDistrictId())
                .ghnWardCode(r.getGhnWardCode())
                .build();
        customerAddress=customerAddressRepository.save(customerAddress);
        return customerAddressMapper.toDto(customerAddress);
    }

    @Transactional
    public CustomerAddressDTO updateCustomerAddress(UpdateCustomerAddressRequest r) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        CustomerAddress customerAddress=customerAddressRepository.findByIdAndUserId(r.getId(),myId)
                .orElseThrow(()->new NotFoundObjectRequestException("Không tồn tại địa chỉ!"));

        customerAddress.setFullName(r.getFullName());
        customerAddress.setPhone(r.getPhone());
        customerAddress.setDetailAddress(r.getDetailAddress());
        customerAddress.setIsDefault(r.getIsDefault());
        customerAddress.setLat(r.getLat());
        customerAddress.setLng(r.getLng());
        // GHN fields
        if (r.getGhnProvinceId() != null) customerAddress.setGhnProvinceId(r.getGhnProvinceId());
        if (r.getGhnDistrictId() != null) customerAddress.setGhnDistrictId(r.getGhnDistrictId());
        if (r.getGhnWardCode() != null)   customerAddress.setGhnWardCode(r.getGhnWardCode());

        customerAddress=customerAddressRepository.save(customerAddress);
        return customerAddressMapper.toDto(customerAddress);
    }

    @Transactional
    public void deleteAddress(Long id) {
        Long myId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        CustomerAddress customerAddress=customerAddressRepository.findByIdAndUserId(id,myId)
                .orElseThrow(()->new NotFoundObjectRequestException("Không tồn tại địa chỉ!"));

        customerAddressRepository.deleteById(id);
    }
}

