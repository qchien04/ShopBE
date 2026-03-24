package com.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VariantStatsDTO {
    private Long   variantId;
    private Long   soldToday;
    private Long   soldThisWeek;
    private Long   soldThisMonth;
    private Long   soldThisYear;
    private Long   soldTotal;
    private Double revenueTotal;
}