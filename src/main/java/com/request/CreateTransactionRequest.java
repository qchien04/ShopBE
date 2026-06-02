package com.request;

import com.constant.PaymentMethod;
import lombok.Data;

@Data
public class CreateTransactionRequest {
    private Long orderId;
    private PaymentMethod paymentMethod;
    private Double amount;
}
