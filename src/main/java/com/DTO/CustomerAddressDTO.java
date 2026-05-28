package com.DTO;

import lombok.Data;

@Data
public class CustomerAddressDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String detailAddress;
    private Double lat;
    private Double lng;
    private Boolean isDefault = false;
    // GHN fields
    private Integer ghnProvinceId;
    private Integer ghnDistrictId;
    private String ghnWardCode;
}
