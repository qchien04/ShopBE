package com.DTO.dashboard;

import com.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─── Donut: đếm theo trạng thái đơn ────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusCountDTO {
    private Order.OrderStatus status;
    private String label;
    private Long count;
    private String color;
}
