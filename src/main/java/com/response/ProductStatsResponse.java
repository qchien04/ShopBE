package com.response;

import com.DTO.VariantStatsDTO;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ProductStatsResponse {
    private Long   productId;
    private String productName;

    private Integer totalViewCount;
    private Integer totalSoldCount;

    private Long   soldToday;
    private Long   soldThisWeek;
    private Long   soldThisMonth;
    private Long   soldThisYear;

    private Double revenueToday;
    private Double revenueThisWeek;
    private Double revenueThisMonth;
    private Double revenueThisYear;

    private List<VariantStatsDTO> variantStats;
}
