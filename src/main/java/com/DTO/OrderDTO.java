package com.DTO;

import com.constant.PaymentMethod;
import com.constant.PaymentStatus;
import com.entity.Order;
import lombok.Data;


import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String customerName;
    private String customerPhone;
    private String shippingAddress;
    private Double subtotal;
    private Double shippingFee;
    private Double actualShippingFee;
    private Double discount;
    private String couponCode;
    private String couponDetails;
    private Double total;
    private Order.OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String note;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;

    private Integer deliveryAttempts;
    private String cancelReason;
    private String internalNote;
    private List<OrderStatusHistoryDTO> statusHistory;

    // GHN shipping integration
    private String ghnOrderCode;
    private LocalDateTime ghnExpectedDeliveryTime;
}

