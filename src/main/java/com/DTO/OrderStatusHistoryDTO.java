package com.DTO;

import com.entity.Order;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatusHistoryDTO {
    private Long id;
    private Order.OrderStatus fromStatus;
    private Order.OrderStatus toStatus;
    private String actionBy;
    private String note;
    private LocalDateTime createdAt;
}
