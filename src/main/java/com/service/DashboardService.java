package com.service;

import com.DTO.dashboard.*;
import com.entity.Order;
import com.entity.PaymentTransaction;
import com.entity.Product;
import com.repository.OrderRepository;
import com.repository.ProductRepository;
import com.repository.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final OrderRepository orderRepository;
        private final ProductRepository productRepository;
        private final UserAccountRepo userRepository;

        // Map màu cho từng trạng thái (khớp với frontend)
        private static final Map<Order.OrderStatus, String> STATUS_COLOR = Map.of(
                        Order.OrderStatus.DELIVERED, "#10b981",
                        Order.OrderStatus.SHIPPING, "#06b6d4",
                        Order.OrderStatus.PROCESSING, "#8b5cf6",
                        Order.OrderStatus.CANCELLED, "#ef4444",
                        Order.OrderStatus.PENDING, "#f59e0b",
                        Order.OrderStatus.CONFIRMED, "#3b82f6",
                        Order.OrderStatus.RETURNED, "#f97316");

        private static final Map<Order.OrderStatus, String> STATUS_LABEL = Map.of(
                        Order.OrderStatus.PENDING, "Chờ xác nhận",
                        Order.OrderStatus.CONFIRMED, "Đã xác nhận",
                        Order.OrderStatus.PROCESSING, "Đang xử lý",
                        Order.OrderStatus.SHIPPING, "Đang giao",
                        Order.OrderStatus.DELIVERED, "Đã giao",
                        Order.OrderStatus.CANCELLED, "Đã huỷ",
                        Order.OrderStatus.RETURNED, "Hoàn hàng");

        // ─── Full dashboard ──────────────────────────────────────────────────────
        public DashboardDTO getDashboard() {
                return DashboardDTO.builder()
                                .stats(buildStats())
                                .revenueByDay(buildRevenueByDay())
                                .orderStatusCounts(buildOrderStatusCounts())
                                .topProducts(buildTopProducts())
                                .recentOrders(buildRecentOrders())
                                .featuredProducts(buildFeaturedProducts())
                                .build();
        }

        // ─── 4 Stat cards ───────────────────────────────────────────────────────
        private StatsDTO buildStats() {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startToday = now.toLocalDate().atStartOfDay();
                LocalDateTime startWeek = now.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();

                // Revenue tháng này vs tháng trước
                int thisMonth = now.getMonthValue();
                int thisYear = now.getYear();
                int prevMonth = thisMonth == 1 ? 12 : thisMonth - 1;
                int prevYear = thisMonth == 1 ? thisYear - 1 : thisYear;

                double revenueThis = orderRepository.sumRevenueByMonth(thisMonth, thisYear);
                double revenuePrev = orderRepository.sumRevenueByMonth(prevMonth, prevYear);
                double revenueGrowth = revenuePrev == 0 ? 0
                                : Math.round(((revenueThis - revenuePrev) / revenuePrev * 100) * 10.0) / 10.0;

                // Orders hôm nay
                long todayOrders = orderRepository.countTodayOrders(startToday);
                long pendingOrders = orderRepository.countPendingOrders();

                // Products
                long totalProducts = productRepository.count();
                long lowStock = productRepository.countLowStock(10);

                // New customers this week
                long newCustomers = userRepository.countNewUsers(startWeek);

                return StatsDTO.builder()
                                .totalRevenue(revenueThis)
                                .revenueGrowthPercent(revenueGrowth)
                                .todayOrders(todayOrders)
                                .pendingOrders(pendingOrders)
                                .orderGrowthPercent(0.0) // có thể tính thêm nếu cần
                                .totalProducts(totalProducts)
                                .lowStockProducts(lowStock)
                                .newCustomersThisWeek(newCustomers)
                                .customerGrowthPercent(0.0)
                                .build();
        }

        // ─── Bar chart: doanh thu 7 ngày ────────────────────────────────────────
        private List<RevenueByDayDTO> buildRevenueByDay() {
                LocalDateTime from = LocalDate.now().minusDays(6).atStartOfDay();
                List<Object[]> rows = orderRepository.revenueGroupByDay(from, PaymentTransaction.PaymentStatus.PAID);

                // Map ngày -> revenue từ DB
                Map<LocalDate, Double> revenueMap = new LinkedHashMap<>();
                for (Object[] row : rows) {
                        LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
                        double rev = ((Number) row[1]).doubleValue();
                        revenueMap.put(date, rev);
                }

                // Đảm bảo đủ 7 ngày kể cả ngày không có đơn
                List<RevenueByDayDTO> result = new ArrayList<>();
                String[] dayLabels = { "T2", "T3", "T4", "T5", "T6", "T7", "CN" };

                for (int i = 6; i >= 0; i--) {
                        LocalDate date = LocalDate.now().minusDays(i);
                        int dow = date.getDayOfWeek().getValue(); // 1=Mon..7=Sun
                        String label = dayLabels[dow - 1];
                        double revenue = revenueMap.getOrDefault(date, 0.0);

                        result.add(RevenueByDayDTO.builder()
                                        .date(date)
                                        .dayLabel(label)
                                        .revenue(revenue)
                                        .build());
                }
                return result;
        }

        // ─── Donut: đếm theo trạng thái ─────────────────────────────────────────
        private List<OrderStatusCountDTO> buildOrderStatusCounts() {
                List<Object[]> rows = orderRepository.countByStatus();
                List<OrderStatusCountDTO> result = new ArrayList<>();

                for (Object[] row : rows) {
                        Order.OrderStatus status = (Order.OrderStatus) row[0];
                        long count = ((Number) row[1]).longValue();
                        result.add(OrderStatusCountDTO.builder()
                                        .status(status)
                                        .label(STATUS_LABEL.getOrDefault(status, status.name()))
                                        .count(count)
                                        .color(STATUS_COLOR.getOrDefault(status, "#9ca3af"))
                                        .build());
                }
                return result;
        }

        // ─── Top 4 sản phẩm bán chạy ────────────────────────────────────────────
        private List<TopProductDTO> buildTopProducts() {
                return productRepository.findTopBySoldCount(PageRequest.of(0, 4))
                                .stream()
                                .map(p -> TopProductDTO.builder()
                                                .id(p.getId())
                                                .name(p.getName())
                                                .sku(p.getSku())
                                                .mainImage(p.getMainImage())
                                                .brand(p.getBrand() != null ? p.getBrand().getName() : null)
                                                .category(p.getCategory() != null ? p.getCategory().getName() : null)
                                                .soldCount(p.getSoldCount())
                                                .price(p.getPrice())
                                                .salePrice(p.getSalePrice())
                                                .build())
                                .toList();
        }

        // ─── 6 đơn hàng gần nhất ────────────────────────────────────────────────
        private List<RecentOrderDTO> buildRecentOrders() {
                return orderRepository.findRecentOrders(PageRequest.of(0, 6))
                                .stream()
                                .map(o -> RecentOrderDTO.builder()
                                                .id(o.getId())
                                                .orderNumber(o.getOrderNumber())
                                                .customerName(o.getCustomerName())
                                                .customerPhone(o.getCustomerPhone())
                                                .total(o.getTotal())
                                                .status(o.getStatus())
                                                .paymentStatus(o.getPaymentStatus() != null
                                                                ? o.getPaymentStatus().name()
                                                                : null)
                                                .paymentMethod(o.getPaymentMethod() != null
                                                                ? o.getPaymentMethod().name()
                                                                : null)
                                                .createdAt(o.getCreatedAt() != null ? o.getCreatedAt().toString()
                                                                : null)
                                                .build())
                                .toList();
        }

        // ─── Sản phẩm nổi bật (table dưới cùng) ─────────────────────────────────
        private List<FeaturedProductDTO> buildFeaturedProducts() {
                return productRepository.findFeatured(PageRequest.of(0, 8))
                                .stream()
                                .map(p -> FeaturedProductDTO.builder()
                                                .id(p.getId())
                                                .name(p.getName())
                                                .sku(p.getSku())
                                                .mainImage(p.getMainImage())
                                                .brand(p.getBrand() != null ? p.getBrand().getName() : null)
                                                .category(p.getCategory() != null ? p.getCategory().getName() : null)
                                                .price(p.getPrice())
                                                .salePrice(p.getSalePrice())
                                                .stockQuantity(p.getStockQuantity())
                                                .soldCount(p.getSoldCount())
                                                .build())
                                .toList();
        }
}