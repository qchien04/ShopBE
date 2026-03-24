package com.DTO.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─── 4 stat cards ───────────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDTO {
    private Double totalRevenue;
    private Double revenueGrowthPercent;   // % so tháng trước

    private Long todayOrders;
    private Long pendingOrders;
    private Double orderGrowthPercent;

    private Long totalProducts;
    private Long lowStockProducts;          // stockQuantity < 10

    private Long newCustomersThisWeek;
    private Double customerGrowthPercent;
}
