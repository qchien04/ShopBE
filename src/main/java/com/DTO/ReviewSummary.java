package com.DTO;


import com.response.PageResponse;

import java.util.List;

public record ReviewSummary(
        Double averageRating,
        Long totalReviews,
        PageResponse<ReviewDTO> reviews,
        List<Long> ratingStatistics
) {}
