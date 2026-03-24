package com.DTO;

import lombok.Data;

@Data
public class CustomerAddressDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String detailAddress;
    private Double lat;
    private Double lng;
    private Boolean isDefault = false;
}
