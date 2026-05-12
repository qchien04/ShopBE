package com.DTO;

import com.entity.Order;
import com.entity.OrderItem;
import com.entity.PaymentTransaction;
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
    private Double discount;
    private String couponCode;
    private String couponDetails;
    private Double total;
    private Order.OrderStatus status;
    private PaymentTransaction.PaymentMethod paymentMethod;
    private PaymentTransaction.PaymentStatus paymentStatus;
    private String note;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;

    private Integer deliveryAttempts;
    private String cancelReason;
    private String internalNote;

}
