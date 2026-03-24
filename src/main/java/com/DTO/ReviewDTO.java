package com.DTO;


import com.entity.Review;

import java.time.LocalDateTime;
public record ReviewDTO(
        Long id,
        String productName,
        String userName,
        String userAvatar,
        Integer rating,
        String comment,
        Review.ReviewStatus reviewStatus,
        LocalDateTime createdAt
) {}
