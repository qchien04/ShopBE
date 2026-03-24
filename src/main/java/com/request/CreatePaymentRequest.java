package com.request;

import com.entity.PaymentTransaction;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Long orderId;
    private PaymentTransaction.PaymentMethod paymentMethod;
}
