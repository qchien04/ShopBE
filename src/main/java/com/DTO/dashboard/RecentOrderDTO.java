package com.DTO.dashboard;

import com.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─── Đơn hàng gần đây ───────────────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderDTO {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private Double total;
    private Order.OrderStatus status;
    private String paymentStatus;
    private String paymentMethod;
    private String createdAt;
}
