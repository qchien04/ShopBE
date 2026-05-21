package com.service;

import com.DTO.ReviewDTO;
import com.DTO.ReviewSummary;
import com.entity.Product;
import com.entity.Review;
import com.entity.User;
import com.exception.InvalidRequestException;
import com.exception.NotFoundObjectRequestException;
import com.repository.ProductRepository;
import com.repository.ReviewRepository;
import com.repository.UserAccountRepo;
import com.repository.OrderItemRepository;
import com.request.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserAccountRepo userRepository;
    private final OrderItemRepository orderItemRepository;

    public ReviewDTO addReview(Long productId, ReviewRequest request) {
        Long userId = ((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (request.rating() < 1 || request.rating() > 5) {
            throw new InvalidRequestException("Rating phải từ 1 đến 5");
        }
        if (reviewRepository.existsByProductIdAndUserId(productId, userId)) {
            throw new InvalidRequestException("Bạn đã đánh giá sản phẩm này rồi");
        }

        if (!orderItemRepository.hasUserPurchasedProduct(userId, productId)) {
            throw new InvalidRequestException("Bạn phải mua sản phẩm này và nhận hàng thành công mới có thể đánh giá");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundObjectRequestException("Không tìm thấy sản phẩm"));
        User user = userRepository.findByUserIdLong(userId)
                .orElseThrow(() -> new NotFoundObjectRequestException("Không tìm thấy người dùng"));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setStatus(Review.ReviewStatus.APPROVED);

        return toResponse(reviewRepository.save(review));
    }

    public ReviewSummary getReviews(Long productId) {
        List<ReviewDTO> reviews = reviewRepository
                .findByProductIdAndStatusOrderByCreatedAtDesc(productId, Review.ReviewStatus.APPROVED)
                .stream()
                .map(this::toResponse)
                .toList();

        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        Long total = reviewRepository.countApprovedByProductId(productId);

        return new ReviewSummary(
                avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0,
                total,
                reviews
        );
    }

    private ReviewDTO toResponse(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getProduct().getName(),
                review.getUser().getFullName(),
                review.getUser().getAvt(),
                review.getRating(),
                review.getComment(),
                review.getStatus(),
                review.getCreatedAt()
        );
    }

    public ReviewDTO approveReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        review.setStatus(Review.ReviewStatus.APPROVED);
        return toResponse(reviewRepository.save(review));
    }

    public ReviewDTO rejectReview(Long reviewId) {
        Review review = getReviewById(reviewId);
        review.setStatus(Review.ReviewStatus.REJECTED);
        return toResponse(reviewRepository.save(review));
    }


    public Page<ReviewDTO> getReviewsByStatus(Review.ReviewStatus status, Pageable pageable) {
        return reviewRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                .map(this::toResponse);
    }

    private Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundObjectRequestException("Không tìm thấy review: " + reviewId));
    }

    public void delete(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}