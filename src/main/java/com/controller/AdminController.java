package com.controller;

import com.DTO.HomePageConfigDTO;
import com.DTO.ReviewDTO;
import com.DTO.ShippingConfigDTO;
import com.DTO.dashboard.DashboardDTO;
import com.entity.Order;
import com.entity.PaymentTransaction;
import com.entity.Review;
import com.response.ApiResponse;
import com.service.ConfigService;
import com.service.DashboardService;
import com.service.ReviewService;
import com.service.implement.OrderService;
import com.entity.Coupon;
import com.service.implement.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final OrderService orderService;
    private final CouponService couponService;
    private final PayOS payOS;
    private final ConfigService configService;
    private final DashboardService dashboardService;
    private final ReviewService reviewService;
    private final com.service.UserAccountService userService;

    // Public — FE homepage gọi
    @GetMapping("/configs/banner")
    public ResponseEntity<HomePageConfigDTO> getBanner() {
        return ResponseEntity.ok(configService.getBannerConfig());
    }

    // Admin only — BannerManager gọi khi nhấn "Lưu tất cả"
    @PostMapping("/configs/banner")
    public ResponseEntity<HomePageConfigDTO> saveBanner(@RequestBody HomePageConfigDTO dto) {
        return ResponseEntity.ok(configService.saveBannerConfig(dto));
    }

    @GetMapping("/configs/shipping")
    public ResponseEntity<ShippingConfigDTO> getShippingConfig() {
        return ResponseEntity.ok(configService.getShippingConfig());
    }

    @PostMapping("/configs/shipping")
    public ResponseEntity<ShippingConfigDTO> saveShippingConfig(@RequestBody ShippingConfigDTO dto) {
        return ResponseEntity.ok(configService.saveShippingConfig(dto));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createCategory() {
        List<Order> unpaidOrders = orderService.findByPaymentStatus(PaymentTransaction.PaymentStatus.UNPAID);

        for (Order order : unpaidOrders) {
            try {
                PaymentLink paymentLink = payOS.paymentRequests()
                        .get(Long.parseLong(order.getOrderNumber().substring(3)));

                if ("PAID".equals(String.valueOf(paymentLink.getStatus()))) {
                    System.out.println("Update "+order.getOrderNumber());
                    orderService.updatePaymentStatus(order.getId(), PaymentTransaction.PaymentStatus.PAID,true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ApiResponse apiResponse=new ApiResponse("ok",true);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/reviews")
    public ResponseEntity<Page<ReviewDTO>> getReviews(
            @RequestParam(defaultValue = "PENDING") Review.ReviewStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getReviewsByStatus(status, pageable));
    }

    @PostMapping("/reviews/{reviewId}/approve")
    public ResponseEntity<ReviewDTO> approve(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.approveReview(reviewId));
    }

    @PostMapping("/reviews/{reviewId}/reject")
    public ResponseEntity<ReviewDTO> reject(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.rejectReview(reviewId));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long reviewId) {
        reviewService.delete(reviewId);
        ApiResponse response=new ApiResponse("Thành công!",true);
        return ResponseEntity.ok(response);
    }

    // --- Coupon Management ---
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/coupons/{id}")
    public ResponseEntity<Coupon> getCoupon(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.getCouponById(id));
    }

    @PostMapping("/coupons")
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        return ResponseEntity.ok(couponService.createCoupon(coupon));
    }

    @PutMapping("/coupons/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon) {
        return ResponseEntity.ok(couponService.updateCoupon(id, coupon));
    }

    @DeleteMapping("/coupons/{id}")
    public ResponseEntity<ApiResponse> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(new ApiResponse("Thành công!", true));
    }

    @GetMapping("/users/new-this-week")
    public ResponseEntity<List<com.DTO.UserAccountDTO>> getNewUsers() {
        return ResponseEntity.ok(userService.getNewUsers());
    }
}
