package com.controller;

import com.DTO.BannerConfigDTO;
import com.DTO.ReviewDTO;
import com.DTO.dashboard.DashboardDTO;
import com.entity.Order;
import com.entity.PaymentTransaction;
import com.entity.Review;
import com.response.ApiResponse;
import com.service.ConfigService;
import com.service.DashboardService;
import com.service.ReviewService;
import com.service.implement.OrderService;
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
    private final PayOS payOS;
    private final ConfigService configService;
    private final DashboardService dashboardService;
    private final ReviewService reviewService;

    // Public — FE homepage gọi
    @GetMapping("/configs/banner")
    public ResponseEntity<BannerConfigDTO> getBanner() {
        return ResponseEntity.ok(configService.getBannerConfig());
    }

    // Admin only — BannerManager gọi khi nhấn "Lưu tất cả"
    @PostMapping("/configs/banner")
    public ResponseEntity<BannerConfigDTO> saveBanner(@RequestBody BannerConfigDTO dto) {
        return ResponseEntity.ok(configService.saveBannerConfig(dto));
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
                    orderService.updatePaymentStatus(order.getId(), PaymentTransaction.PaymentStatus.PAID);
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
}
