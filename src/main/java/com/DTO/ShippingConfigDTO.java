package com.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingConfigDTO {
    private Integer ghnProvinceId;
    private Integer ghnDistrictId;
    private String ghnWardCode;
    
    private String provinceName;
    private String districtName;
    private String wardName;
    private String detailAddress;
}
