package com.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// ─── Doanh thu 7 ngày ───────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByDayDTO {
    private LocalDate date;
    private String dayLabel;   // "T2", "T3"...
    private Double revenue;
}
