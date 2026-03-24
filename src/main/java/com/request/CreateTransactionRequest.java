package com.request;

import com.entity.PaymentTransaction;
import lombok.Data;

@Data
public class CreateTransactionRequest {
    private Long orderId;
    private PaymentTransaction.PaymentMethod paymentMethod;
    private Double amount;
}
