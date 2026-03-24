package com.request;

public record ReviewRequest(
        Integer rating,   // 1-5
        String comment
) {}