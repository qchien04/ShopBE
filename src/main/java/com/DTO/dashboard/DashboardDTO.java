package com.DTO.dashboard;

import com.entity.Order;
import com.entity.Product;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    private StatsDTO stats;
    private List<RevenueByDayDTO> revenueByDay;
    private List<OrderStatusCountDTO> orderStatusCounts;
    private List<TopProductDTO> topProducts;
    private List<RecentOrderDTO> recentOrders;
    private List<FeaturedProductDTO> featuredProducts;
}

