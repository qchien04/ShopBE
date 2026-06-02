package com.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
public class AdminOrderFilterRequest {
    private int page = 0;
    private int size = 5;
    private String status = "ALL";
    private String keyword;
    private String paymentStatus; // null = ALL

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;
}