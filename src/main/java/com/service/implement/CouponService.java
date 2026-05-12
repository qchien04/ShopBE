package com.service.implement;
import com.entity.*;
import com.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    public List<Coupon> getActiveCoupons() {
        return couponRepository.findActiveCoupons(LocalDateTime.now());
    }

    public Coupon validateCoupon(String code, Double orderTotal) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getActive()) {
            throw new RuntimeException("Coupon is inactive");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            throw new RuntimeException("Coupon is expired");
        }

        if (orderTotal < coupon.getMinOrderValue()) {
            throw new RuntimeException("Order value too low for this coupon");
        }

        if (coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Coupon usage limit reached");
        }

        return coupon;
    }

    public Double calculateDiscount(Coupon coupon, Double orderTotal) {
        Double discount;
        if (coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE) {
            discount = orderTotal * (coupon.getDiscountValue() / 100);
        } else {
            discount = coupon.getDiscountValue();
        }

        if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount() > 0) {
            discount = Math.min(discount, coupon.getMaxDiscountAmount());
        }

        return Math.min(discount, orderTotal);
    }

    @Transactional
    public void useCoupon(String code) {
        Coupon coupon = couponRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        if (!coupon.getActive()) {
            throw new RuntimeException("Coupon is inactive");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getStartDate()) || now.isAfter(coupon.getEndDate())) {
            throw new RuntimeException("Coupon is expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public Coupon getCouponById(Long id) {
        return couponRepository.findById(id).orElseThrow(() -> new RuntimeException("Coupon not found"));
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new RuntimeException("Coupon code is required");
        }
        if (couponRepository.findByCode(coupon.getCode()).isPresent()) {
            throw new RuntimeException("Coupon code already exists");
        }
        coupon.setCreatedAt(LocalDateTime.now());
        coupon.setUsedCount(0);
        if (coupon.getActive() == null) {
            coupon.setActive(true);
        }
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, Coupon updateInfo) {
        Coupon coupon = getCouponById(id);
        
        if (updateInfo.getCode() != null && !updateInfo.getCode().equals(coupon.getCode())) {
            if (couponRepository.findByCode(updateInfo.getCode()).isPresent()) {
                throw new RuntimeException("Coupon code already exists");
            }
            coupon.setCode(updateInfo.getCode());
        }
        
        coupon.setDescription(updateInfo.getDescription());
        coupon.setDiscountType(updateInfo.getDiscountType());
        coupon.setDiscountValue(updateInfo.getDiscountValue());
        coupon.setMinOrderValue(updateInfo.getMinOrderValue());
        coupon.setMaxDiscountAmount(updateInfo.getMaxDiscountAmount());
        coupon.setUsageLimit(updateInfo.getUsageLimit());
        coupon.setStartDate(updateInfo.getStartDate());
        coupon.setEndDate(updateInfo.getEndDate());
        coupon.setActive(updateInfo.getActive());
        
        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }
}
