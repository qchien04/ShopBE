package com.request;

import com.entity.Order.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
    private String      reason;
    private String      internalNote;
}
