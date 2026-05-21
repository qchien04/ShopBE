package com.repository;

import com.entity.Order;
import com.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
        Optional<Order> findByOrderNumber(String orderNumber);

        List<Order> findByUserId(Long userId);

        @Query("SELECT o FROM Order o " +
                        "join fetch o.items i " +
                        "where o.id=:orderID and o.user.id=:userID")
        Optional<Order> findByIdWithFullItem(Long orderID, Long userID);

        @Query("SELECT o FROM Order o " +
                        "join fetch o.items i " +
                        "where o.user.id=:userId ")
        List<Order> findFullInfoByUserId(Long userId);

        @Query("SELECT o FROM Order o " +
                        "join fetch o.items i " +
                        "ORDER BY DATE(o.createdAt) DESC ")
        List<Order> findAllWithFullInfo();

        List<Order> findByStatus(Order.OrderStatus status);

        List<Order> findByPaymentStatus(PaymentTransaction.PaymentStatus status);

        @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
        List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tổng doanh thu trong khoảng thời gian (chỉ đơn DELIVERED)
        @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
                        "WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :from AND :to")
        Double sumRevenueBetween(@Param("from") LocalDateTime from,
                        @Param("to") LocalDateTime to);

        // Đếm đơn theo ngày (cho bar chart 7 ngày)
        @Query("SELECT DATE(o.createdAt) AS day, COALESCE(SUM(o.total), 0) AS revenue " +
                        "FROM Order o " +
                        "WHERE o.paymentStatus =:status AND o.createdAt >= :from " +
                        "GROUP BY DATE(o.createdAt) " +
                        "ORDER BY DATE(o.createdAt)")
        List<Object[]> revenueGroupByDay(@Param("from") LocalDateTime from,
                        @Param("status") PaymentTransaction.PaymentStatus status);

        // Đếm đơn theo từng trạng thái
        @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
        List<Object[]> countByStatus();

        // Đếm đơn hôm nay
        @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startOfDay")
        Long countTodayOrders(@Param("startOfDay") LocalDateTime startOfDay);

        // Đếm đơn PENDING/PROCESSING
        @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN ('PENDING','PROCESSING','CONFIRMED')")
        Long countPendingOrders();

        // Đơn hàng gần nhất
        @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
        List<Order> findRecentOrders(org.springframework.data.domain.Pageable pageable);

        // So sánh doanh thu 2 tháng (growth)
        @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o " +
                        "WHERE o.status = 'DELIVERED' " +
                        "AND MONTH(o.createdAt) = :month AND YEAR(o.createdAt) = :year")
        Double sumRevenueByMonth(@Param("month") int month, @Param("year") int year);
}
