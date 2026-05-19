package com.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private Order.OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Order.OrderStatus toStatus;

    private String actionBy; // "ADMIN", "USER", "SYSTEM", etc.

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
