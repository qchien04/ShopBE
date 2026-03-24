package com.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "payment_refunds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentRefund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã hoàn tiền
    @Column(unique = true, nullable = false)
    private String refundCode;

    // Giao dịch gốc
    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private PaymentTransaction transaction;

    // Đơn hàng
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    // Số tiền hoàn
    @Column(nullable = false)
    private Double refundAmount;

    // Lý do
    private String reason;

    // Trạng thái
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;

    // Người yêu cầu
    @ManyToOne
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public enum RefundStatus {
        PENDING,      // Chờ xử lý
        APPROVED,     // Đã duyệt
        COMPLETED,    // Hoàn thành
        REJECTED      // Từ chối
    }
}