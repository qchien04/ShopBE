package com.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@Builder
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String orderNumber;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String customerName;
    private String customerPhone;
    private String shippingAddress;

    private Double subtotal;
    private Double shippingFee;
    private Double discount = 0.0;
    private Double total;

    @Column(columnDefinition = "TEXT")
    private String cancelReason;

    @Column(columnDefinition = "TEXT")
    private String internalNote;

    private Integer deliveryAttempts = 0;

    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentTransaction.PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentTransaction.PaymentStatus paymentStatus = PaymentTransaction.PaymentStatus.UNPAID;

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private Set<OrderItem> items = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING,
        SHIPPING, DELIVERED, CANCELLED, DELIVERY_FAILED, RETURNED
    }
}
