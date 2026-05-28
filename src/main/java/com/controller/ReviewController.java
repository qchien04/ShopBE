package com.controller;
import com.DTO.ReviewDTO;
import com.DTO.ReviewSummary;
import com.request.ReviewRequest;
import com.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
@RestController

@RequestMapping("/reviews/products/{productId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // Lấy danh sách review
    @GetMapping
    public ResponseEntity<ReviewSummary> getReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reviewService.getReviews(productId, pageable));
    }

    // Thêm review (cần đăng nhập)
    @PostMapping
    public ResponseEntity<ReviewDTO> addReview(
            @PathVariable Long productId,
            @RequestBody ReviewRequest request
    ) {
        return ResponseEntity.ok(reviewService.addReview(productId, request));
    }
}