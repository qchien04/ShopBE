package com.controller;

import com.entity.Coupon;
import com.request.CouponValidationRequest;
import com.response.CouponValidationResponse;
import com.service.implement.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @RequestBody CouponValidationRequest request) {
        try {
            Coupon coupon = couponService.validateCoupon(request.getCode(), request.getOrderTotal());
            Double discount = couponService.calculateDiscount(coupon, request.getOrderTotal());

            CouponValidationResponse response = new CouponValidationResponse();
            response.setValid(true);
            response.setCoupon(coupon);
            response.setDiscountAmount(discount);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            CouponValidationResponse response = new CouponValidationResponse();
            response.setValid(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/use")
    public ResponseEntity<Void> useCoupon(@RequestParam String code) {
        couponService.useCoupon(code);
        return ResponseEntity.ok().build();
    }
}
