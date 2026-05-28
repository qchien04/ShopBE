package com.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GHNConfig {

    @Value("${ghn.api-url}")
    private String apiUrl;

    @Value("${ghn.token}")
    private String token;

    @Value("${ghn.shop-id}")
    private Integer shopId;

    @Value("${ghn.service-type-id:2}")
    private Integer serviceTypeId;

    /** Quận/huyện của cửa hàng (lấy từ GHN master data) – dùng trong calculate-fee */
    @Value("${ghn.from-district-id:0}")
    private Integer fromDistrictId;

    /** Phường/xã của cửa hàng (wardCode từ GHN) – dùng trong calculate-fee */
    @Value("${ghn.from-ward-code:}")
    private String fromWardCode;
}
