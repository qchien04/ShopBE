package com.response;

import com.entity.Coupon;
import lombok.Data;

@Data
public class CouponValidationResponse {
    private boolean valid;
    private Coupon coupon;
    private Double discountAmount;
    private String message;
}
