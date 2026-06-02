package com.request;

import com.constant.PaymentMethod;
import lombok.Data;

@Data
public class CreatePaymentRequest {
    private Long orderId;
    private PaymentMethod paymentMethod;
}
