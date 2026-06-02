package com.repository;

import com.constant.PaymentStatus;
import com.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
        Optional<Order> findByOrderNumber(String orderNumber);

        @Query(value = """
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.productVariant
            WHERE (:status IS NULL OR o.status = :status)
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
              AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
              AND (:toDate IS NULL OR o.createdAt <= :toDate)
            """,
                        countQuery = """
            SELECT COUNT(o) FROM Order o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
              AND (:fromDate IS NULL OR o.createdAt >= :fromDate)
              AND (:toDate IS NULL OR o.createdAt <= :toDate)
            """)
        Page<Order> findByStatusWithFilters(
                @Param("status") Order.OrderStatus status,
                @Param("keyword") String keyword,
                @Param("paymentStatus") PaymentStatus paymentStatus,
                @Param("fromDate") LocalDateTime fromDate,
                @Param("toDate") LocalDateTime toDate,
                Pageable pageable
        );

        @Query(value = """
            SELECT o.id FROM orders o
            WHERE (:status IS NULL OR o.status = CAST(:status AS VARCHAR))
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(o.order_number) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customer_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customer_phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:paymentStatus IS NULL OR o.payment_status = CAST(:paymentStatus AS VARCHAR))
              AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR o.created_at >= CAST(:fromDate AS TIMESTAMP))
              AND (CAST(:toDate AS TIMESTAMP) IS NULL OR o.created_at <= CAST(:toDate AS TIMESTAMP))
            ORDER BY o.created_at DESC
            """,
                        countQuery = """
            SELECT COUNT(o.id) FROM orders o
            WHERE (:status IS NULL OR o.status = CAST(:status AS VARCHAR))
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(o.order_number) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customer_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(o.customer_phone) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:paymentStatus IS NULL OR o.payment_status = CAST(:paymentStatus AS VARCHAR))
              AND (CAST(:fromDate AS TIMESTAMP) IS NULL OR o.created_at >= CAST(:fromDate AS TIMESTAMP))
              AND (CAST(:toDate AS TIMESTAMP) IS NULL OR o.created_at <= CAST(:toDate AS TIMESTAMP))
            """,
                nativeQuery = true)
        Page<Long> findIdsByFilters(
                @Param("status") String status,
                @Param("keyword") String keyword,
                @Param("paymentStatus") String paymentStatus,
                @Param("fromDate") LocalDateTime fromDate,
                @Param("toDate") LocalDateTime toDate,
                Pageable pageable
        );

        @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.productVariant
            WHERE o.id IN :ids
            ORDER BY o.createdAt DESC
            """)
        List<Order> findByIdsWithFullInfo(@Param("ids") List<Long> ids);

        @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.productVariant")
        Page<Order> findAllWithPageable(Pageable pageable);

        Page<Order> findByStatusIn(List<Order.OrderStatus> statuses, Pageable pageable);


        Page<Order> findByUserId(Long userId, Pageable pageable);

        Page<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status, Pageable pageable);

        Page<Order> findByUserIdAndStatusIn(Long userId, List<Order.OrderStatus> statuses, Pageable pageable);

        @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.user.id = :userId GROUP BY o.status")
        List<Object[]> countByStatusForUser(@Param("userId") Long userId);

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

        Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

        List<Order> findByPaymentStatus(PaymentStatus status);

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
                        @Param("status") PaymentStatus status);

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
