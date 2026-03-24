package com.DTO;


import java.util.List;

public record ReviewSummary(
        Double averageRating,
        Long totalReviews,
        List<ReviewDTO> reviews
) {}
