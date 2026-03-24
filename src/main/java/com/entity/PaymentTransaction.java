package com.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mã giao dịch
    @Column(unique = true, nullable = false)
    private String transactionCode;

    // Đơn hàng
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Phương thức thanh toán
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    // Số tiền
    @Column(nullable = false)
    private Double amount;

    // Mã giao dịch từ cổng thanh toán (VNPay, MoMo...)
    private String gatewayTransactionId;

    // Trạng thái
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    // Thông tin response từ cổng thanh toán
    @Column(columnDefinition = "TEXT")
    private String responseData;

    // Ghi chú
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public enum TransactionStatus {
        PENDING,      // Đang chờ
        SUCCESS,      // Thành công
        FAILED,       // Thất bại
        CANCELLED     // Đã hủy
    }

    public enum PaymentStatus { UNPAID, PAID, REFUNDED }


    public enum PaymentMethod {
        BANK_TRANSFER, MOMO, VNPAY, ZALOPAY, COD
    }
}