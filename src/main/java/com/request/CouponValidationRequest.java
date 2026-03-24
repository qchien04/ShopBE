package com.request;

import lombok.Data;

@Data
public class CouponValidationRequest {
    private String code;
    private Double orderTotal;
}
